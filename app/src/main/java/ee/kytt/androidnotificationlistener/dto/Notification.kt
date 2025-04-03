package ee.kytt.androidnotificationlistener.dto

import android.app.Notification.EXTRA_BIG_TEXT
import android.app.Notification.EXTRA_SUB_TEXT
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.service.notification.StatusBarNotification
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Int,
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val category: String,
    val time: Long
) {
    constructor(sbn: StatusBarNotification) : this(
        id = sbn.id,
        packageName = sbn.packageName,
        title = sbn.notification.extras.getString(EXTRA_TITLE, ""),
        text = sbn.notification.extras.getCharSequence(EXTRA_TEXT, "").toString(),
        subText = sbn.notification.extras.getCharSequence(EXTRA_SUB_TEXT, "").toString(),
        bigText = sbn.notification.extras.getCharSequence(EXTRA_BIG_TEXT, "").toString(),
        category = sbn.notification.category ?: "",
        time = sbn.postTime
    )

    fun description(): String {
        return listOf(title, text, subText, bigText).firstOrNull { it.isNotBlank() } ?: ""
    }

}
