package ee.kytt.androidnotificationlistener.service

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_CALLBACK_URL
import ee.kytt.androidnotificationlistener.Constants.PREF_FAIL_COUNT
import ee.kytt.androidnotificationlistener.Constants.PREF_LAST_SUCCESS_TIME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_ATTEMPT_TIME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_PACKAGE_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_STATUS
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_TITLE
import ee.kytt.androidnotificationlistener.Constants.PREF_PACKAGE_PATTERN
import ee.kytt.androidnotificationlistener.data.Notification
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
        val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val url = prefs.getString(PREF_CALLBACK_URL, null)
        val packagePattern = prefs.getString(PREF_PACKAGE_PATTERN, null) ?: ""

        Log.d("NotificationListener", Json.Default.encodeToString(notification))

        if (!shouldMatch(packagePattern, notification.packageName)) {
            Log.d("NotificationListener", "Ignoring notification from package: ${notification.packageName}")
            return
        }

        if (url.isNullOrEmpty()) {
            Log.w("NotificationListener", "No callback URL set")
            saveFailedNotification(notification, context, "No callback URL")
            return
        }

        callbackService.sendAsync(url, notification) { result ->
            if (!result.success) {
                saveFailedNotification(notification, context, result.status)
            } else {
                prefs.edit().apply {
                    putString(PREF_LATEST_TITLE, notification.description())
                    putString(PREF_LATEST_PACKAGE_NAME, notification.packageName)
                    putString(PREF_LATEST_STATUS, result.status)
                    putLong(PREF_LATEST_ATTEMPT_TIME, System.currentTimeMillis())
                    putLong(PREF_LAST_SUCCESS_TIME, System.currentTimeMillis())
                    apply()
                }
            }
        }
    }

    private fun saveFailedNotification(notification: Notification, context: Context, status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = NotificationDatabase.Companion.getDatabase(context)
            db.notificationDao().insert(notification)
            val failed = db.notificationDao().count()
            Log.d("NotificationListener", "Saved failed notification to local DB, unsynced count: $failed")
            context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
                putInt(PREF_FAIL_COUNT, failed)
                putString(PREF_LATEST_STATUS, status)
                putLong(PREF_LATEST_ATTEMPT_TIME, System.currentTimeMillis())
                apply()
            }
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
