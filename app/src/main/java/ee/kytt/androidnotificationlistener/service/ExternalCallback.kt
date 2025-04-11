package ee.kytt.androidnotificationlistener.service

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

class ExternalCallback {

    private val client = OkHttpClient()

    fun sendNotificationToServer(
        url: String,
        notification: Notification,
        callback: (Boolean, String) -> Unit
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
                Log.d("ExternalCallback", "Response: $response")
                val status = if (response.isSuccessful) "Success" else "Failed: ${response.message}"
                callback(response.isSuccessful, status)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("ExternalCallback", "Failed to post notification", e)
                callback(false, "Failed: ${e.message}" )
            }
        })
    }

}
