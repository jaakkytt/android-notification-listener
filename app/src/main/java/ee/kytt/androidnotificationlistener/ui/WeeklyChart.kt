package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.mikephil.charting.data.BarEntry
import ee.kytt.androidnotificationlistener.Constants.PREFS_NAME
import ee.kytt.androidnotificationlistener.Constants.PREF_LATEST_ATTEMPT_TIME
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.persistence.NotificationDatabase
import ee.kytt.androidnotificationlistener.ui.element.Chart
import ee.kytt.androidnotificationlistener.ui.theme.Green
import ee.kytt.androidnotificationlistener.ui.theme.Red
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun WeeklyChart(
    context: Context,
    modifier: Modifier = Modifier
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    val entriesState = remember { mutableStateOf<List<BarEntry>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_LATEST_ATTEMPT_TIME) {
                coroutineScope.launch {
                    entriesState.value = generateEntries(context)
                }
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        entriesState.value = generateEntries(context)
    }

    val dayLabels = listOf(
        R.string.weekday_short_mon,
        R.string.weekday_short_tue,
        R.string.weekday_short_wed,
        R.string.weekday_short_thu,
        R.string.weekday_short_fri,
        R.string.weekday_short_sat,
        R.string.weekday_short_sun
    ).map { stringResource(it) }

    val todayIndex = LocalDate.now().dayOfWeek.value % 7
    val rotatedDayLabels = dayLabels.drop(todayIndex) + dayLabels.take(todayIndex)

    Chart(modifier, rotatedDayLabels, entriesState.value, listOf(Green, Red))
}

suspend fun generateEntries(context: Context): List<BarEntry> {
    val db = NotificationDatabase.getDatabase(context)
    val dao = db.notificationDao()

    val startDay = LocalDate.now().minusDays(6).toEpochDay()
    val chartData = dao.getChartData(startDay)

    val dayToEntry = chartData.associateBy { it.day }

    return (0..6).map { i ->
        val day = LocalDate.now().minusDays((6 - i).toLong()).toEpochDay()
        val data = dayToEntry[day]
        BarEntry(
            i.toFloat(),
            floatArrayOf(
                data?.syncedCount?.toFloat() ?: 0f,
                data?.unsyncedCount?.toFloat() ?: 0f
            )
        )
    }
}
