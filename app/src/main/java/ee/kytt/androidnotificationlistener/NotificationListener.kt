package ee.kytt.androidnotificationlistener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class NotificationListener : NotificationListenerService() {

    private val client = OkHttpClient()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val category = notification.category

        Log.d("NotificationListener", """
            Notification from: $packageName
            Title: $title
            Text: $text
            SubText: $subText
            BigText: $bigText
            Category: $category
        """.trimIndent())

        sendNotificationToServer(
            packageName,
            title,
            text,
            subText,
            bigText,
            category
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification Removed: " + sbn.packageName)
    }

    private fun sendNotificationToServer(
        packageName: String,
        title: String?,
        text: String?,
        subText: String?,
        bigText: String?,
        category: String?
    ) {
        val json = JSONObject().apply {
            put("package", packageName)
            put("title", title ?: "")
            put("text", text ?: "")
            put("subText", subText ?: "")
            put("bigText", bigText ?: "")
            put("category", category ?: "")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://f440-85-253-101-13.ngrok-free.app/send/logs")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotificationPoster", "Failed to post notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("NotificationPoster", "Response: ${response.code}")
            }
        })
    }

}
