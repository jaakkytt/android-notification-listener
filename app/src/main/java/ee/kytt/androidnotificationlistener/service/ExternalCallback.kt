package ee.kytt.androidnotificationlistener.service

import android.util.Log
import ee.kytt.androidnotificationlistener.data.Notification
import ee.kytt.androidnotificationlistener.data.SyncResult
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

    fun sendAsync(
        url: String,
        token: String,
        notification: Notification,
        callback: (SyncResult) -> Unit
    ) {
        val request: Request

        try {
            request = createRequest(url, token, notification)
        } catch (e: Exception) {
            Log.w("ExternalCallback", "Failed to create request", e)
            callback(SyncResult(e))
            return
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    Log.d("ExternalCallback", "Response: $response")
                    callback(SyncResult(response))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("ExternalCallback", "Failed to post notification", e)
                callback(SyncResult(e))
            }
        })
    }

    fun sendSync(url: String, token: String, notification: Notification): SyncResult {
        try {
            val request = createRequest(url, token, notification)
            client.newCall(request).execute().use { response ->
                Log.d("ExternalCallback", "Response: $response")
                return SyncResult(response)
            }
        } catch (e: Exception) {
            Log.w("ExternalCallback", "Failed to post notification", e)
            return SyncResult(e)
        }
    }

    private fun createRequest(url: String, token: String, notification: Notification): Request {
        val json = Json.encodeToString(notification)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val builder = Request.Builder()
            .url(url)
            .post(body)

        if (token.isNotEmpty()) {
            builder.addHeader("Authorization", "Bearer $token")
        }

        return builder.build()
    }

}
