package com.example.saveuplite

import android.app.Application
import com.example.saveuplite.ui.utils.NotificationHelper

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}