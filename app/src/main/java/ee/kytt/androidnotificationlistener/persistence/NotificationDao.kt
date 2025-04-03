package ee.kytt.androidnotificationlistener.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ee.kytt.androidnotificationlistener.dto.Notification

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: Notification)

    @Query("SELECT * FROM failed_notifications")
    suspend fun getAll(): List<Notification>

    @Query("SELECT count(*) FROM failed_notifications")
    suspend fun count(): Int

    @Query("DELETE FROM failed_notifications")
    suspend fun clearAll()

}
