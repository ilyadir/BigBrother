package com.bigbrother.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bigbrother.app.ui.AppNavHost
import com.bigbrother.app.ui.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var launchRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRoute = intent.resolveRoute()
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppNavHost(initialRoute = launchRoute ?: Routes.Onboarding)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchRoute = intent.resolveRoute()
    }

    private fun Intent.resolveRoute(): String? {
        return when {
            getStringExtra(EXTRA_DESTINATION) == DEST_TOP_UP -> Routes.TopUp
            getStringExtra(EXTRA_DESTINATION) == DEST_TIMER -> Routes.Timer
            data?.host.equals(DEEP_LINK_HOST_TOP_UP, ignoreCase = true) -> Routes.TopUp
            data?.host.equals(DEEP_LINK_HOST_TIMER, ignoreCase = true) -> Routes.Timer
            else -> null
        }
    }

    companion object {
        private const val EXTRA_DESTINATION = "extra_destination"
        private const val DEST_TOP_UP = "topup"
        private const val DEST_TIMER = "timer"
        private const val DEEP_LINK_HOST_TOP_UP = "topup"
        private const val DEEP_LINK_HOST_TIMER = "timer"

        fun createTopUpIntent(context: Context): Intent {
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse("bigbrother://$DEEP_LINK_HOST_TOP_UP"),
                context,
                MainActivity::class.java
            )
                .putExtra(EXTRA_DESTINATION, DEST_TOP_UP)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
        }

        fun createTimerIntent(context: Context): Intent {
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse("bigbrother://$DEEP_LINK_HOST_TIMER"),
                context,
                MainActivity::class.java
            )
                .putExtra(EXTRA_DESTINATION, DEST_TIMER)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
        }
    }
}
