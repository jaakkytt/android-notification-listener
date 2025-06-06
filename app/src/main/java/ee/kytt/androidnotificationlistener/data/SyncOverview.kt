package ee.kytt.androidnotificationlistener.data

data class SyncOverview(
    val day: Long,
    val syncedCount: Int,
    val unsyncedCount: Int
)
