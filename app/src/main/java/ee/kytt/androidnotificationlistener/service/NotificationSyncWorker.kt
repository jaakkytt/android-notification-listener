package ee.kytt.androidnotificationlistener.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log
import ee.kytt.androidnotificationlistener.persistence.NotificationDatabase

class NotificationSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val maxAttempts = 3
    private val callbackService = ExternalCallback()

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val url = prefs.getString("callback_url", null)

        if (url.isNullOrEmpty()) {
            Log.w("NotificationSyncWorker", "No callback URL set")
            return Result.failure()
        }

        Log.d("NotificationSyncWorker", "Starting sync with URL: $url attempt $runAttemptCount")

        val db = NotificationDatabase.getDatabase(applicationContext)
        val dao = db.notificationDao()
        val failedNotifications = dao.getAll()
        var failedCount = failedNotifications.size

        for (notification in failedNotifications) {
            val response = callbackService.sendSync(url, notification)

            if (response.success) {
                dao.delete(notification.id)
                Log.d("NotificationSyncWorker", "Resent and deleted notification: ${notification.id}")
                prefs.edit().apply {
                    putString("latestTitle", notification.description())
                    putString("latestPackageName", notification.packageName)
                    putString("latestStatus", response.status)
                    putLong("latestAttemptTime", System.currentTimeMillis())
                    putLong("lastSuccessTime", System.currentTimeMillis())
                    apply()
                }
                failedCount -= 1
            } else {
                Log.w("NotificationSyncWorker", "Failed to resend notification: ${notification.id}, status: ${response.status}")

                prefs.edit().apply {
                    putInt("failCount", failedCount)
                    putString("latestStatus", response.status)
                    putLong("latestAttemptTime", System.currentTimeMillis())
                    apply()
                }

                if (runAttemptCount < maxAttempts) {
                    return Result.retry()
                }

                Log.w("NotificationSyncWorker", "Max retry limit reached ($maxAttempts attempts)")
                return Result.failure()
            }
        }

        return Result.success()
    }

}
