package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ee.kytt.androidnotificationlistener.service.NotificationSyncWorker
import java.util.concurrent.TimeUnit.MINUTES

@Composable
fun BackgroundSyncButton(
    context: Context,
    modifier: Modifier
) {
    val syncEnabledState = remember { mutableStateOf(isSyncEnabled(context)) }

    Button(
        onClick = {
            toggleBackgroundSync(context)
            syncEnabledState.value = !syncEnabledState.value
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (syncEnabledState.value) "Background Sync: Enabled" else "Background Sync: Disabled",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Switch(
                checked = syncEnabledState.value,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

fun isSyncEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("sync_enabled", false)
}

fun toggleBackgroundSync(context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val currentlyEnabled = isSyncEnabled(context)

    if (currentlyEnabled) {
        WorkManager.getInstance(context).cancelUniqueWork("notification_sync")
    } else {
        val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "notification_sync",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    prefs.edit() { putBoolean("sync_enabled", !currentlyEnabled) }
}
