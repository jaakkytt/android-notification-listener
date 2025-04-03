package ee.kytt.androidnotificationlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import ee.kytt.androidnotificationlistener.dto.Notification
import kotlinx.serialization.json.Json


class NotificationListener : NotificationListenerService() {

    private val callbackService = ee.kytt.androidnotificationlistener.service.Callback()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = Notification(sbn)

        Log.d("NotificationListener", Json.encodeToString(notification))

        val context = applicationContext
        val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val url = prefs.getString("callback_url", null)

        if (url.isNullOrEmpty()) {
            Log.w("NotificationListener", "No callback URL set")
            return
        }

        callbackService.sendNotificationToServer(url, notification, context)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification Removed: " + sbn.packageName)
    }

}
