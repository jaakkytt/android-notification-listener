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
        val context = applicationContext
        val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val url = prefs.getString("callback_url", null)
        val packagePattern = prefs.getString("package_pattern", null) ?: ""

        Log.d("NotificationListener", Json.Default.encodeToString(notification))

        if (!shouldMatch(packagePattern, notification.packageName)) {
            Log.d("NotificationListener", "Ignoring notification from package: ${notification.packageName}")
            return
        }

        if (url.isNullOrEmpty()) {
            Log.w("NotificationListener", "No callback URL set")
            saveFailedNotification(notification, context)
            return
        }

        callbackService.sendAsync(url, notification) { result ->
            if (!result.success) {
                saveFailedNotification(notification, context)
            }

            prefs.edit().apply {
                putString("latestTitle", notification.description())
                putString("latestPackageName", notification.packageName)
                putString("latestStatus", result.status)
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

    private fun shouldMatch(packagePattern: String, name: String): Boolean {
        if (packagePattern.isBlank()) {
            return true
        }
        return try {
            return Regex(packagePattern).containsMatchIn(name)
        } catch (e: Exception) {
            Log.w("NotificationListener", "Invalid regex pattern: $packagePattern", e)
            false
        }
    }

}
