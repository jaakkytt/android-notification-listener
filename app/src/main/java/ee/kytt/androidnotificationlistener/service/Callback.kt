package ee.kytt.androidnotificationlistener.service

import android.content.Context
import android.util.Log
import ee.kytt.androidnotificationlistener.dto.Notification
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


class Callback {

    private val client = OkHttpClient()

    fun sendNotificationToServer(
        url: String,
        notification: Notification,
        context: Context
    ) {
        val json = Json.encodeToString(notification)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

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
                    putString("latestTitle", notification.description())
                    putString("latestPackageName", notification.packageName)
                    putString("latestStatus", status)
                    apply()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotificationPoster", "Failed to post notification", e)
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("latestTitle", notification.description())
                    putString("latestPackageName", notification.packageName)
                    putString("latestStatus", "Failed: ${e.message}")
                    apply()
                }
            }
        })
    }

}
