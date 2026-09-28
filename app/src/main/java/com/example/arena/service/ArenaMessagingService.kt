package com.example.arena.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.arena.MainActivity
import com.example.arena.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ArenaMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val channelId = message.data["channel"] ?: "user_alerts"
        val title = message.notification?.title ?: message.data["title"] ?: "Arena Notification"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val reservaId = message.data["reserva_id"] ?: ""
        val sedeId = message.data["sede_id"]

        val notificationId = if (reservaId.isNotEmpty()) reservaId.hashCode() else System.currentTimeMillis().toInt()

        showNotification(channelId, title, body, notificationId, sedeId)
    }

    private fun showNotification(
        channelId: String,
        title: String,
        body: String,
        notificationId: Int,
        sedeId: String? = null
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (channelId == "admin_alerts") "Alertas Administrador" else "Alertas Usuario"
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_screen", if (channelId == "admin_alerts") "ADMIN_PAYMENTS" else "USER_RESERVATIONS")
            if (sedeId != null) putExtra("sede_id", sedeId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_padel_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
