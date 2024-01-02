package com.arcxp.thearcxp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL


class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage.notification != null) {
            showNotification(
                remoteMessage.notification!!.title!!,
                remoteMessage.notification!!.body,
                remoteMessage.notification!!.imageUrl,
                remoteMessage.data
            )
        }
    }

    private fun showNotification(
        title: String,
        message: String?,
        imageUrl: Uri?,
        data: Map<String, String>?
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        data?.forEach {
            val key = it.key
            val value = it.value
            if (value != null) {
                intent.putExtra(key, value as String)
            }
        }
        val requestCode = 0
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            flags
        )

        val image = getBitmapfromUrl(imageUrl.toString())

        val channelId = "arcxp_mobile_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        if (image != null) {
            notificationBuilder
                .setLargeIcon(image)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
        }

        //notificationBuilder.setContent(getCustomDesign(title, message!!))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("TheArcXP", "Error in getting notification image: " + e.localizedMessage)
            null
        }
    }

    // Save for later in case we do customized notifications
//    private fun getCustomDesign(
//        title: String,
//        message: String
//    ): RemoteViews {
//        val remoteViews = RemoteViews(
//            ApplicationProvider.getApplicationContext<Context>().getPackageName(),
//            R.layout.notification
//        )
//        remoteViews.setTextViewText(R.id.title, title)
//        remoteViews.setTextViewText(R.id.message, message)
//        remoteViews.setImageViewResource(
//            R.id.icon,
//            R.drawable.gfg
//        )
//        return remoteViews
//    }
}