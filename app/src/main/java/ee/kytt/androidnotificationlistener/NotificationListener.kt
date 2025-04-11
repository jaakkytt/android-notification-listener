package ee.kytt.androidnotificationlistener

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import ee.kytt.androidnotificationlistener.dto.Notification
import ee.kytt.androidnotificationlistener.service.ExternalCallback
import ee.kytt.androidnotificationlistener.persistence.NotificationDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NotificationListener : NotificationListenerService() {

    private val callbackService = ExternalCallback()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = Notification(sbn)

        Log.d("NotificationListener", Json.Default.encodeToString(notification))

        val context = applicationContext
        val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val url = prefs.getString("callback_url", null)

        if (url.isNullOrEmpty()) {
            Log.w("NotificationListener", "No callback URL set")
            return
        }

        callbackService.sendNotificationToServer(url, notification) { success, status ->
            if (!success) {
                saveFailedNotification(notification, context)
            }

            prefs.edit().apply {
                putString("latestPackageName", notification.packageName)
                putString("latestTitle", notification.description())
                putString("latestStatus", status)
                apply()
            }
        }
    }

    private fun saveFailedNotification(notification: Notification, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = NotificationDatabase.getDatabase(context)
            db.notificationDao().insert(notification)
            val failed = db.notificationDao().count()
            Log.d("NotificationListener", "Saved failed notification to local DB, unsynced count: $failed")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification Removed: " + sbn.packageName)
    }

}
