package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SyncStatusText(
    context: Context
) {
    val prefs = context.getSharedPreferences("app_prefs", ComponentActivity.MODE_PRIVATE)

    val latestTitle = remember { mutableStateOf(prefs.getString("latestTitle", "") ?: "") }
    val latestPackage = remember { mutableStateOf(prefs.getString("latestPackageName", "-") ?: "-") }
    val latestStatus = remember { mutableStateOf(prefs.getString("latestStatus", "-") ?: "-") }
    val latestAttemptTime = remember { mutableLongStateOf(prefs.getLong("latestAttemptTime", 0L)) }
    val lastSuccessTime = remember { mutableLongStateOf(prefs.getLong("lastSuccessTime", 0L)) }
    val failCount = remember { mutableIntStateOf(prefs.getInt("failCount", 0)) }

    val listener = remember {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "latestTitle" -> latestTitle.value = prefs.getString("latestTitle", "") ?: ""
                "latestPackageName" -> latestPackage.value = prefs.getString("latestPackageName", "-") ?: "-"
                "latestStatus" -> latestStatus.value = prefs.getString("latestStatus", "-") ?: "-"
                "latestAttemptTime" -> latestAttemptTime.longValue = prefs.getLong("latestAttemptTime", 0L)
                "lastSuccessTime" -> lastSuccessTime.longValue = prefs.getLong("lastSuccessTime", 0L)
                "failCount" -> failCount.intValue = prefs.getInt("failCount", 0)
            }
        }
    }

    DisposableEffect(Unit) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isSuccess = latestStatus.value.startsWith("Success", ignoreCase = true)
    val statusColor = if (isSuccess) Color.Green else Color.Red

    @Composable
    fun StatusText(text: String) {
        Text(
            text = text,
            color = statusColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (isSuccess) {
        StatusText("Last synced: ${formatTimestamp(lastSuccessTime.longValue)}")
        StatusText(latestPackage.value)
        StatusText(latestTitle.value)
    } else {
        StatusText("Unsynced entries: ${failCount.intValue}")
        StatusText("Latest attempt: ${formatTimestamp(latestAttemptTime.longValue)}")
        StatusText("Last synced: ${formatTimestamp(lastSuccessTime.longValue)}")
    }
}

fun formatTimestamp(millis: Long): String {
    if (millis <= 0) {
        return "never"
    }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(millis))
}
