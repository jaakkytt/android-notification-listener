package ee.kytt.androidnotificationlistener.data

import android.app.Notification.EXTRA_BIG_TEXT
import android.app.Notification.EXTRA_SUB_TEXT
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.service.notification.StatusBarNotification
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
@Entity(tableName = "notifications", primaryKeys = ["id"])
data class Notification(
    val id: String,
    val packageName: String,
    val key: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val category: String,
    val time: Long,

    @Transient
    @ColumnInfo(name = "day")
    var day: Long = 0L,

    @Transient
    @ColumnInfo(name = "synchronized")
    val synchronized: Boolean = false
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
        key = sbn.key,
        title = sbn.notification.extras.getString(EXTRA_TITLE, ""),
        text = sbn.notification.extras.getCharSequence(EXTRA_TEXT, "").toString(),
        subText = sbn.notification.extras.getCharSequence(EXTRA_SUB_TEXT, "").toString(),
        bigText = sbn.notification.extras.getCharSequence(EXTRA_BIG_TEXT, "").toString(),
        category = sbn.notification.category ?: "",
        time = sbn.postTime,
        day = Instant.ofEpochMilli(sbn.postTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
    )

    fun description(): String {
        return listOf(title, text, subText, bigText).firstOrNull { it.isNotBlank() } ?: ""
    }

}
