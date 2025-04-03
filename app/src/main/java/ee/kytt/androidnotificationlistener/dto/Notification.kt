package ee.kytt.androidnotificationlistener.dto

import java.security.MessageDigest
import android.app.Notification.EXTRA_BIG_TEXT
import android.app.Notification.EXTRA_SUB_TEXT
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.service.notification.StatusBarNotification
import androidx.room.Entity
import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
@Entity(tableName = "failed_notifications", primaryKeys = ["id"])
data class Notification(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val category: String,
    val time: Long
) {
    companion object {
        fun hashId(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    constructor(sbn: StatusBarNotification) : this(
        id = hashId("${sbn.packageName}|${sbn.key}|${sbn.postTime}"),
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
