package com.oezzdeen.android_video_streaming.fcm.services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.oezzdeen.android_video_streaming.R
import com.oezzdeen.android_video_streaming.ui.activities.MainActivity
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "my_channel"
        private const val NOTIFICATION_REQUEST_CODE = 0

        private const val mTAG = "_MessagingService"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(mTAG, "onMessageReceived: from => ${message.from}")
        Log.d(mTAG, "onMessageReceived: data => ${message.data}")

        // Check if message contains a notification payload. [Notification Message]
        if (message.notification != null) {
            Log.d(mTAG, "onMessageReceived: title => ${message.notification?.title}")
            Log.d(mTAG, "onMessageReceived: body => ${message.notification?.body}")

            buildNotification(message.notification?.title, message.notification?.body)

        }

        // Check if message contains a data payload. [Data Message]
        if (message.data.isNotEmpty()) {
            buildNotification(message.data["title"], message.data["body"])
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(mTAG, "onNewToken: $token")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(notificationManager: NotificationManager) {
        val channelName = "Channel Name"
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            this.description = "My channel description"
            this.enableLights(true)
            this.lightColor = Color.GREEN
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun buildNotification(notificationTitle: String?, notificationBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationManager = notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setSmallIcon(R.drawable.ic_notifications_active_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }
}