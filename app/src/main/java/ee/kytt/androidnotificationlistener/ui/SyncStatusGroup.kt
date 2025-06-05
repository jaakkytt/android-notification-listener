package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_FAIL_COUNT
import ee.kytt.androidnotificationlistener.Constants.PREF_LAST_SUCCESS_TIME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_ATTEMPT_TIME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_PACKAGE_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_STATUS
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_TITLE
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.ui.element.ContentGroup
import ee.kytt.androidnotificationlistener.ui.theme.Green
import ee.kytt.androidnotificationlistener.ui.theme.Red
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SyncStatusGroup(
    context: Context
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

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

    var labelLastSynced = stringResource(R.string.last_synced)
    var labelUnsyncedEntries = stringResource(R.string.unsynced_entries)
    var labelLatestAttempt = stringResource(R.string.latest_attempt)
    var labelDatetimeFormat = stringResource(R.string.datetime_format)
    var labelDatetimeNever = stringResource(R.string.datetime_never)

    DisposableEffect(Unit) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isSuccess = latestStatus.value.startsWith("Success", ignoreCase = true)
    val statusColor = if (isSuccess) Green else Red

    @Composable
    fun StatusText(text: String) {
        Text(
            text = text,
            color = statusColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    ContentGroup(title = stringResource(R.string.sync_status)) {
        if (isSuccess) {
            StatusText(
                "${labelLastSynced}: ${
                    formatTimestamp(
                        lastSuccessTime.longValue,
                        labelDatetimeNever,
                        labelDatetimeFormat
                    )
                }"
            )
            StatusText(latestPackage.value)
            StatusText(latestTitle.value)
        } else {
            StatusText("${labelUnsyncedEntries}: ${failCount.intValue}")
            StatusText(
                "${labelLatestAttempt}: ${
                    formatTimestamp(
                        latestAttemptTime.longValue,
                        labelDatetimeNever,
                        labelDatetimeFormat
                    )
                }"
            )
            StatusText(
                "${labelLastSynced}: ${
                    formatTimestamp(
                        lastSuccessTime.longValue,
                        labelDatetimeNever,
                        labelDatetimeFormat
                    )
                }"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            SyncNowButton(context, Modifier.fillMaxWidth())
        }
    }

}

fun formatTimestamp(millis: Long, labelNever: String, datetimeFormat: String): String {
    if (millis <= 0) {
        return labelNever
    }
    val formatter = DateTimeFormatter.ofPattern(datetimeFormat).withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(millis))
}
