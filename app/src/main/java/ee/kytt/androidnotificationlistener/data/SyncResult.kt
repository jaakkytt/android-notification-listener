package ee.kytt.androidnotificationlistener.data

import okhttp3.Response

data class SyncResult(
    val success: Boolean,
    val status: String
) {

    constructor(response: Response) : this(
        success = response.isSuccessful,
        status = if (response.isSuccessful) "Success" else "Failed: ${response.message}"
    )

    constructor(e: Exception) : this(
        success = false,
        status = "Failed: ${e.message}"
    )

}
