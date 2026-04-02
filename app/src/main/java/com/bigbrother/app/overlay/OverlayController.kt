package com.bigbrother.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.bigbrother.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val windowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private var overlayView: ComposeView? = null
    private var shownForPackage: String? = null

    fun show(blockedPackageName: String) {
        if (overlayView != null && shownForPackage == blockedPackageName) return
        hide()

        val view = ComposeView(context).apply {
            setContent {
                OverlayContent(
                    blockedAppName = blockedPackageName,
                    onTopUpClick = {
                        hide()
                        context.startActivity(MainActivity.createTopUpIntent(context))
                    },
                    onStartFocusClick = {
                        hide()
                        context.startActivity(MainActivity.createTimerIntent(context))
                    },
                    onCloseClick = ::hide
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(view, params)
        overlayView = view
        shownForPackage = blockedPackageName
    }

    fun hide() {
        overlayView?.let(windowManager::removeViewImmediate)
        overlayView = null
        shownForPackage = null
    }
}

@Composable
private fun OverlayContent(
    blockedAppName: String,
    onTopUpClick: () -> Unit,
    onStartFocusClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Баланс исчерпан",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                text = "Заблокировано приложение: $blockedAppName",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTopUpClick
            ) {
                Text(text = "Добавить время")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                onClick = onStartFocusClick
            ) {
                Text(text = "Начать фокус-сессию")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                onClick = onCloseClick
            ) {
                Text(text = "Закрыть")
            }
        }
    }
}
