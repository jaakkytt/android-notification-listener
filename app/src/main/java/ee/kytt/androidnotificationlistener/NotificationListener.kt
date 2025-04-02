package ee.kytt.androidnotificationlistener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val category = notification.category

        Log.d("NotifListener", """
            Notification from: $packageName
            Title: $title
            Text: $text
            SubText: $subText
            BigText: $bigText
            Category: $category
        """.trimIndent())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotifListener", "Notification Removed: " + sbn.packageName)
    }

}
