package ee.kytt.androidnotificationlistener.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ee.kytt.androidnotificationlistener.data.Notification
import ee.kytt.androidnotificationlistener.data.SyncOverview

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: Notification)

    @Query("SELECT * FROM notifications WHERE synchronized = 0 ORDER BY time ASC")
    suspend fun getFailed(): List<Notification>

    @Query("SELECT count(*) FROM notifications WHERE synchronized = 0 ")
    suspend fun countFailed(): Int

    @Query("UPDATE notifications SET synchronized = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM notifications WHERE time < :thresholdTime AND synchronized = 1")
    suspend fun deleteOlderThan(thresholdTime: Long)

    @Query("""
        SELECT day,
               SUM(CASE WHEN synchronized = 1 THEN 1 ELSE 0 END) as syncedCount,
               SUM(CASE WHEN synchronized = 0 THEN 1 ELSE 0 END) as unsyncedCount
        FROM notifications
        WHERE day >= :startDay
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getChartData(startDay: Long): List<SyncOverview>

}
