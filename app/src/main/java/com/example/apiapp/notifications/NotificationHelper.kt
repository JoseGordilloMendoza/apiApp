package com.example.apiapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.apiapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sincronización",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos sobre la sincronización de datos offline"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun notifySyncComplete(newItemsFound: Boolean) {
        val title = if (newItemsFound) "Nuevos personajes disponibles" else "Conexión restaurada"
        val text = if (newItemsFound) {
            "Se sincronizaron nuevos personajes para verlos sin conexión"
        } else {
            "Tus datos guardados están actualizados"
        }
        notify(title, text, NotificationCompat.PRIORITY_DEFAULT)
    }

    fun notifyDisconnected() {
        notify(
            title = "Sin conexión",
            text = "Trabajando con los datos guardados localmente",
            priority = NotificationCompat.PRIORITY_LOW
        )
    }

    private fun notify(title: String, text: String, priority: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "sync_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
