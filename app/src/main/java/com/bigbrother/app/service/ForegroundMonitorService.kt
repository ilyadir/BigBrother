package com.bigbrother.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ForegroundMonitorService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
