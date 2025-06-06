package ee.kytt.androidnotificationlistener.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ee.kytt.androidnotificationlistener.persistence.NotificationDatabase
import java.time.LocalDate
import java.time.ZoneId

class CleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cutoff = LocalDate.now()
            .minusDays(7)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val db = NotificationDatabase.getDatabase(applicationContext)
        val dao = db.notificationDao()

        dao.deleteOlderThan(cutoff)

        Log.d("CleanupWorker", "Deleted old notifications before $cutoff")

        return Result.success()
    }

}
