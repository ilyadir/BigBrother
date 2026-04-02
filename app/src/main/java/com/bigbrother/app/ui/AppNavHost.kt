package com.bigbrother.app.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bigbrother.app.ui.balance.BalanceScreen

object Routes {
    const val Onboarding = "Onboarding"
    const val Balance = "Balance"
    const val TopUp = "TopUp"
    const val TrackedApps = "TrackedApps"
    const val Timer = "Timer"
}

@Composable
fun AppNavHost(
    initialRoute: String = Routes.Onboarding,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = initialRoute
    ) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                isHardMode = false,
                onContinue = {
                    navController.navigate(Routes.Balance) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Balance) {
            BalanceScreen()
        }
        composable(Routes.TopUp) {
            TopUpScreen()
        }
        composable(Routes.TrackedApps) {
            TrackedAppsScreen()
        }
        composable(Routes.Timer) {
            TimerScreen()
        }
    }
}

@Composable
fun OnboardingScreen(
    isHardMode: Boolean,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var usageGranted by remember { mutableStateOf(context.hasUsageStatsPermission()) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var accessibilityGranted by remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }

    fun refreshPermissions() {
        usageGranted = context.hasUsageStatsPermission()
        overlayGranted = Settings.canDrawOverlays(context)
        accessibilityGranted = context.isAccessibilityServiceEnabled()
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val mandatoryGranted = usageGranted && overlayGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Onboarding", style = MaterialTheme.typography.headlineMedium)

        PermissionCard(
            title = "Usage Access",
            granted = usageGranted,
            onClick = {
                settingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )

        PermissionCard(
            title = "Overlay Permission",
            granted = overlayGranted,
            onClick = {
                val uri = "package:${context.packageName}".toUri()
                settingsLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri))
            }
        )

        if (isHardMode) {
            PermissionCard(
                title = "Accessibility",
                granted = accessibilityGranted,
                onClick = {
                    settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onContinue,
            enabled = mandatoryGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Continue to Balance")
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    granted: Boolean,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (granted) "Granted" else "Not granted",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onClick) {
                Text(text = "Open")
            }
        }
    }
}

private fun Context.hasUsageStatsPermission(): Boolean {
    val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun Context.isAccessibilityServiceEnabled(): Boolean {
    val enabledServices = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices
        .split(':')
        .any { it.substringBefore('/').equals(packageName, ignoreCase = true) }
}

@Composable
fun TopUpScreen() = Text(text = "TopUp")

@Composable
fun TrackedAppsScreen() = Text(text = "TrackedApps")

@Composable
fun TimerScreen() = Text(text = "Focus timer")
