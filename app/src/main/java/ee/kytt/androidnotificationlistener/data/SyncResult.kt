package ee.kytt.androidnotificationlistener.data

import okhttp3.Response

data class SyncResult(
    val success: Boolean,
    val status: String,
    val userMessage: String,
) {

    constructor(response: Response) : this(
        success = response.isSuccessful,
        status = if (response.isSuccessful) "Success" else "Failed: ${response.message}",
        userMessage = if (response.isSuccessful) "" else "Server [${response.code}]: ${response.message}"
    )

    constructor(e: Exception) : this(
        success = false,
        status = "Failed: ${e.message}",
        userMessage = "App: ${e.message}"
    )

}
