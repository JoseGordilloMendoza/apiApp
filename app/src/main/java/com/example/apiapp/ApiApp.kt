package com.example.apiapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.apiapp.data.sync.SyncScheduler
import com.example.apiapp.notifications.NotificationHelper
import com.example.apiapp.shortcuts.AppShortcuts
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApiApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannel()
        SyncScheduler.schedulePeriodicSync(this)
        AppShortcuts.register(this)
    }
}
