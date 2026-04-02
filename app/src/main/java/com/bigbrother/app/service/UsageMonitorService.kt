package com.bigbrother.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bigbrother.app.data.local.dao.TrackedAppDao
import com.bigbrother.app.domain.repository.BalanceRepository
import com.bigbrother.app.overlay.OverlayController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UsageMonitorService : LifecycleService() {

    @Inject
    lateinit var trackedAppDao: TrackedAppDao

    @Inject
    lateinit var balanceRepository: BalanceRepository

    @Inject
    lateinit var overlayController: OverlayController

    private var monitoringJob: Job? = null
    private var pendingSpendSeconds: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    private fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        monitoringJob = lifecycleScope.launch {
            while (true) {
                runMonitoringTick()
                delay(TICK_MS)
            }
        }
    }

    private suspend fun runMonitoringTick() {
        val foregroundPackage = withContext(Dispatchers.Default) { findForegroundPackage() }
        val blockedPackages = trackedAppDao.listBlocked().first().map { it.packageName }.toSet()

        if (foregroundPackage != null && foregroundPackage in blockedPackages) {
            val balanceMinutes = balanceRepository.getAvailableMinutes().first()
            if (balanceMinutes > 0) {
                pendingSpendSeconds += TICK_SECONDS
                overlayController.hide()
                flushSpendIfNeeded()
            } else {
                overlayController.show(resolveAppLabel(foregroundPackage))
            }
        } else {
            overlayController.hide()
        }
    }



    private fun resolveAppLabel(packageName: String): String {
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName)
    }
    private suspend fun flushSpendIfNeeded() {
        val minutesToSpend = pendingSpendSeconds / SECONDS_PER_MINUTE
        if (minutesToSpend <= 0) return
        pendingSpendSeconds %= SECONDS_PER_MINUTE
        balanceRepository.addSpend(minutesToSpend, "Auto spend by monitoring")
    }

    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        overlayController.hide()
    }

    private fun findForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - EVENT_LOOKBACK_MS, now)
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val movedToForeground = event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            val resumed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            if (movedToForeground || resumed) {
                foregroundPackage = event.packageName
            }
        }
        return foregroundPackage
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("BigBrother monitoring")
            .setContentText("Наблюдение за активным приложением")
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "monitoring"
        private const val NOTIFICATION_ID = 101
        private const val EVENT_LOOKBACK_MS = 15_000L
        private const val TICK_SECONDS = 5
        private const val TICK_MS = TICK_SECONDS * 1_000L
        private const val SECONDS_PER_MINUTE = 60

        private const val ACTION_START = "com.bigbrother.app.service.START_MONITORING"
        fun start(context: Context) {
            context.startForegroundService(
                Intent(context, UsageMonitorService::class.java).setAction(ACTION_START)
            )
            MonitoringPreferences.setEnabled(context, true)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, UsageMonitorService::class.java))
            MonitoringPreferences.setEnabled(context, false)
        }
    }
}

object MonitoringPreferences {
    private const val PREFS_NAME = "monitoring_prefs"
    private const val KEY_ENABLED = "monitoring_enabled"

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
