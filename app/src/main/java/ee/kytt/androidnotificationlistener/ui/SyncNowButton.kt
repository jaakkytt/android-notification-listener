package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ee.kytt.androidnotificationlistener.service.NotificationSyncWorker

@Composable
fun SyncNowButton(
    context: Context,
    modifier: Modifier
) {
    Button(
        onClick = {
            triggerOneTimeNotificationSync(context)
            Toast.makeText(context, "Sync started", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier
    ) {
        Text("Sync Failed Notifications Now")
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
        "manual_sync",
        ExistingWorkPolicy.KEEP,
        request
    )
}
