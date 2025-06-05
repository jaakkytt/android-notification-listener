package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_SYNC_ERROR
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.ui.element.ContentGroup
import ee.kytt.androidnotificationlistener.ui.theme.Red

@Composable
fun LatestErrorGroup(
    context: Context
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    val latestSyncError = remember { mutableStateOf(prefs.getString(PREF_LATEST_SYNC_ERROR, "") ?: "") }

    val listener = remember {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PREF_LATEST_SYNC_ERROR -> latestSyncError.value = prefs.getString(PREF_LATEST_SYNC_ERROR, "") ?: ""
            }
        }
    }

    DisposableEffect(Unit) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    if (latestSyncError.value.isNotEmpty()) {
        ContentGroup(title = stringResource(R.string.latest_sync_error)) {
            Text(
                text = latestSyncError.value,
                color = Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
