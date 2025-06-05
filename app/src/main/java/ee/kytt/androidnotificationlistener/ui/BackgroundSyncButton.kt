package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ee.kytt.androidnotificationlistener.Constants.BACKGROUND_WORK_NAME
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_SYNC_ENABLED
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.service.NotificationSyncWorker
import ee.kytt.androidnotificationlistener.ui.element.SettingSwitch
import java.util.concurrent.TimeUnit.MINUTES

@Composable
fun BackgroundSyncButton(context: Context) {
    val syncEnabledState = remember { mutableStateOf(isSyncEnabled(context)) }

    SettingSwitch(
        title = stringResource(R.string.background_sync_enable_title),
        checked = syncEnabledState.value,
        description = stringResource(R.string.background_sync_enable_description),
        onClick = {
            toggleBackgroundSync(context)
            syncEnabledState.value = !syncEnabledState.value
        }
    )
}

fun isSyncEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(PREF_SYNC_ENABLED, false)
}

fun toggleBackgroundSync(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val currentlyEnabled = isSyncEnabled(context)

    if (currentlyEnabled) {
        WorkManager.getInstance(context).cancelUniqueWork(BACKGROUND_WORK_NAME)
    } else {
        val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BACKGROUND_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    prefs.edit() { putBoolean(PREF_SYNC_ENABLED, !currentlyEnabled) }
}
