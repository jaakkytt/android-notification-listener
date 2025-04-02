package ee.kytt.androidnotificationlistener

import android.app.Notification
import android.content.Context
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

        val context = applicationContext
        val prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE)
        val url = prefs.getString("callback_url", null)

        if (url.isNullOrEmpty()) {
            Log.w("NotificationListener", "No callback URL set")
            return
        }

        sendNotificationToServer(
            url,
            packageName,
            title,
            text,
            subText,
            bigText,
            category,
            context
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification Removed: " + sbn.packageName)
    }

    private fun sendNotificationToServer(
        url: String,
        packageName: String,
        title: String?,
        text: String?,
        subText: String?,
        bigText: String?,
        category: String?,
        context: Context
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
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("NotificationPoster", "Response: ${response.code}")
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val status = if (response.code == 200) "Success" else "Fail"
                prefs.edit().apply {
                    putString("latestTitle", text ?: title)
                    putString("latestPackageName", packageName)
                    putString("latestStatus", status)
                    apply()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotificationPoster", "Failed to post notification", e)
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("latestTitle", text ?: title)
                    putString("latestPackageName", packageName)
                    putString("latestStatus", "Failed: ${e.message}")
                    apply()
                }
            }
        })
    }

}
