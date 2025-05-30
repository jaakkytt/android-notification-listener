package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ee.kytt.androidnotificationlistener.Constants.MANUAL_WORK_NAME
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.service.NotificationSyncWorker

@Composable
fun SyncNowButton(
    context: Context,
    modifier: Modifier
) {
    var labelSyncStarted = stringResource(R.string.sync_started)
    var labelTriggerSyncManually = stringResource(R.string.trigger_sync_manually)

    Button(
        onClick = {
            triggerOneTimeNotificationSync(context)
            Toast.makeText(context, labelSyncStarted, Toast.LENGTH_SHORT).show()
        },
        modifier = modifier
    ) {
        Text(labelTriggerSyncManually)
    }
}

fun triggerOneTimeNotificationSync(context: Context) {
    val request = OneTimeWorkRequestBuilder<NotificationSyncWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        MANUAL_WORK_NAME,
        ExistingWorkPolicy.KEEP,
        request
    )
}
