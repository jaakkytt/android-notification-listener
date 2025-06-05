package ee.kytt.androidnotificationlistener.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.mikephil.charting.data.BarEntry
import ee.kytt.androidnotificationlistener.R
import ee.kytt.androidnotificationlistener.ui.element.Chart
import ee.kytt.androidnotificationlistener.ui.theme.Green
import ee.kytt.androidnotificationlistener.ui.theme.Red
import java.time.LocalDate
import kotlin.random.Random

@Composable
fun WeeklyChart(
    context: Context,
    modifier: Modifier = Modifier
) {
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

    val fakeData = List(7) {
        val synced = Random.nextInt(0, 50)
        val unsynced = Random.nextInt(0, 30)
        floatArrayOf(synced.toFloat(), unsynced.toFloat())
    }

    val entries = fakeData.mapIndexed { index, (synced, unsynced) ->
        BarEntry(index.toFloat(), floatArrayOf(synced, unsynced))
    }

    Chart(modifier, rotatedDayLabels, entries, listOf(Green, Red))
}
