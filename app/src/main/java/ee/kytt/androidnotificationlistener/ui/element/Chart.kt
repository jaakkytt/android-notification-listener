package ee.kytt.androidnotificationlistener.ui.element

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    xLabels: List<String>,
    entries: List<BarEntry>,
    entryColors: List<Color>
) {
    val color = MaterialTheme.colorScheme.primary.toArgb()
    val size = MaterialTheme.typography.labelSmall.fontSize.value

    val dataSet = BarDataSet(entries, "Notifications").apply {
        colors = entryColors.map { it.toArgb() }
        setDrawValues(false)
    }

    val barData = BarData(dataSet).apply {
        barWidth = 0.5f
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(160.dp).padding(0.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                this.data = barData
                description.isEnabled = false
                setFitBars(true)
                setDrawValueAboveBar(true)
                setTouchEnabled(false)
                animateY(800)
                legend.isEnabled = false

                axisLeft.apply {
                    axisMinimum = 0f
                    granularity = 1f
                    textColor = color
                    setDrawGridLines(false)
                }

                axisRight.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = color
                    textSize = size
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index in xLabels.indices) xLabels[index] else ""
                        }
                    }
                }

                invalidate()
            }
        },
        update = { chart ->
            chart.data = barData
            chart.invalidate()
        }
    )

}
