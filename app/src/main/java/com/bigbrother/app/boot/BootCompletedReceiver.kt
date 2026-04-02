package com.bigbrother.app.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bigbrother.app.service.MonitoringPreferences
import com.bigbrother.app.service.UsageMonitorService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val safeContext = context ?: return
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (MonitoringPreferences.isEnabled(safeContext)) {
            UsageMonitorService.start(safeContext)
        }
    }
}
