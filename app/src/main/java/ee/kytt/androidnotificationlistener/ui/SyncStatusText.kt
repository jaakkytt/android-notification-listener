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
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_FAIL_COUNT
import ee.kytt.androidnotificationlistener.Constants.PREF_LAST_SUCCESS_TIME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_ATTEMPT_TIME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_PACKAGE_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_STATUS
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_TITLE
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SyncStatusText(
    context: Context
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, ComponentActivity.MODE_PRIVATE)

    val latestTitle = remember { mutableStateOf(prefs.getString(PREF_LATEST_TITLE, "") ?: "") }
    val latestPackage = remember { mutableStateOf(prefs.getString(PREF_LATEST_PACKAGE_NAME, "-") ?: "-") }
    val latestStatus = remember { mutableStateOf(prefs.getString(PREF_LATEST_STATUS, "-") ?: "-") }
    val latestAttemptTime = remember { mutableLongStateOf(prefs.getLong(PREF_LATEST_ATTEMPT_TIME, 0L)) }
    val lastSuccessTime = remember { mutableLongStateOf(prefs.getLong(PREF_LAST_SUCCESS_TIME, 0L)) }
    val failCount = remember { mutableIntStateOf(prefs.getInt(PREF_FAIL_COUNT, 0)) }

    val listener = remember {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PREF_LATEST_TITLE -> latestTitle.value = prefs.getString(PREF_LATEST_TITLE, "") ?: ""
                PREF_LATEST_PACKAGE_NAME -> latestPackage.value = prefs.getString(PREF_LATEST_PACKAGE_NAME, "-") ?: "-"
                PREF_LATEST_STATUS -> latestStatus.value = prefs.getString(PREF_LATEST_STATUS, "-") ?: "-"
                PREF_LATEST_ATTEMPT_TIME -> latestAttemptTime.longValue = prefs.getLong(PREF_LATEST_ATTEMPT_TIME, 0L)
                PREF_LAST_SUCCESS_TIME -> lastSuccessTime.longValue = prefs.getLong(PREF_LAST_SUCCESS_TIME, 0L)
                PREF_FAIL_COUNT -> failCount.intValue = prefs.getInt(PREF_FAIL_COUNT, 0)
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
