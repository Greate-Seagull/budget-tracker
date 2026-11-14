package com.lichsu.budget_tracker.ui.stats

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.lichsu.budget_tracker.R
import com.lichsu.budget_tracker.data.local.dao.CategoryTotal
import com.lichsu.budget_tracker.data.local.dao.DailyTotal
import com.lichsu.budget_tracker.util.MoneyFormat
import java.time.YearMonth
import kotlin.math.roundToLong

@Composable
internal fun CategoryPieChart(categories: List<CategoryTotal>, colors: List<Color>, total: Long) {
    val foreground = MaterialTheme.colorScheme.onSecondary.toArgb()
    val background = MaterialTheme.colorScheme.secondary.toArgb()
    val label = stringResource(R.string.tv3_by_category)
    val totalText = MoneyFormat.vnd(total)
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(224.dp),
        factory = { context ->
            PieChart(context).apply {
                legend.isEnabled = false
                description.isEnabled = false
                setDrawEntryLabels(false)
                isRotationEnabled = false
                setTouchEnabled(false)
                holeRadius = 65f
                transparentCircleRadius = 65f
                setCenterTextSize(16f)
            }
        },
        update = { chart ->
            val slices = categories.map { PieEntry((it.total.toDouble() / total).toFloat()) }
            val dataSet = PieDataSet(slices, label).apply {
                setColors(colors.map { it.toArgb() })
                setDrawValues(false)
            }
            chart.setHoleColor(background)
            chart.setCenterTextColor(foreground)
            chart.centerText = totalText
            chart.contentDescription = "$label. $totalText"
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
    )
}

@Composable
internal fun DailyBarChart(month: YearMonth, daily: List<DailyTotal>) {
    val textColor = MaterialTheme.colorScheme.onSecondary.toArgb()
    val barColor = MaterialTheme.colorScheme.primary.toArgb()
    val label = stringResource(R.string.tv3_daily_totals)
    val dailyByDay = daily.associate { it.date.dayOfMonth to it.total }
    val daysInMonth = month.lengthOfMonth()
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(230.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(false)
                setFitBars(true)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                xAxis.setLabelCount(6, false)
                axisLeft.axisMinimum = 0f
                axisLeft.granularity = 1f
                axisLeft.setLabelCount(4, false)
                axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String =
                        MoneyFormat.number(value.toDouble().roundToLong())
                }
            }
        },
        update = { chart ->
            val entries = (1..daysInMonth).map { day ->
                BarEntry(day.toFloat(), (dailyByDay[day] ?: 0L).toFloat())
            }
            val dataSet = BarDataSet(entries, label).apply {
                color = barColor
                setDrawValues(false)
            }
            chart.axisLeft.textColor = textColor
            chart.xAxis.textColor = textColor
            chart.contentDescription = label + ". " + daily.joinToString { day ->
                "${day.date.dayOfMonth}: ${MoneyFormat.vnd(day.total)}"
            }
            chart.data = BarData(dataSet).apply { barWidth = 0.72f }
            chart.invalidate()
        },
    )
}
