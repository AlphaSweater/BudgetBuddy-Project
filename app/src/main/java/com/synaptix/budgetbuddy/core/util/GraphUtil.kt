package com.synaptix.budgetbuddy.core.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.extentions.getThemeColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object GraphUtil {
    private const val TAG = "GraphUtil"

    //================================================================================
    // Line Chart Methods
    //================================================================================
    fun setupLineChart(
        chart: LineChart,
        transactions: List<Transaction>,
        chartType: String,
        dateRange: ClosedRange<Long>
    ) {
        val context = chart.context
        chart.clear()
        chart.setNoDataText("No ${chartType} data available")
        chart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))

        Log.d(TAG, "Date range: ${Date(dateRange.start)} to ${Date(dateRange.endInclusive)}")

        // Create a list of all days in the date range
        val allDates = generateDateRange(dateRange.start, dateRange.endInclusive)

        // Sort transactions by date
        val sortedTransactions = transactions.sortedBy { it.date }

        // Set line color based on chart type
        val lineColor = when (chartType) {
            "income" -> ContextCompat.getColor(context, R.color.profit_green)
            else -> ContextCompat.getColor(context, R.color.expense_red)
        }

        // Group transactions by day
        val dailyTransactions = sortedTransactions.groupBy { transaction ->
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        Log.d(TAG, "Transaction dates: ${dailyTransactions.keys.map { Date(it) }}")

        // Prepare data entries and x-axis labels
        val entries = mutableListOf<Entry>()
        val xAxisLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Track running totals for each day
        val dailyTotals = mutableMapOf<Long, Double>()

        // Initialize all days with zero
        allDates.forEach { date ->
            dailyTotals[date] = 0.0
        }

        // Calculate daily totals
        dailyTransactions.forEach { (date, dayTransactions) ->
            val dailyTotal = dayTransactions.sumOf { it.amount.toDouble() }
            dailyTotals[date] = dailyTotal
            Log.d(TAG, "Date ${Date(date)} has total: $dailyTotal")
        }

        // Calculate cumulative totals for all days
        var runningTotal = 0.0
        allDates.forEachIndexed { index, date ->
            runningTotal += dailyTotals[date] ?: 0.0
            entries.add(Entry(index.toFloat(), runningTotal.toFloat()))

            // Only add label if it's the start date, end date, or has transactions
            val hasTransactions = dailyTotals[date] != 0.0
            val isStartDate = isSameDay(date, dateRange.start)
            val isEndDate = isSameDay(date, dateRange.endInclusive)

            val shouldShowLabel = isStartDate || isEndDate || hasTransactions

            Log.d(TAG, "Date ${Date(date)} - Start: $isStartDate, End: $isEndDate, HasTransactions: $hasTransactions, ShowLabel: $shouldShowLabel")

            xAxisLabels.add(if (shouldShowLabel) dateFormat.format(Date(date)) else "")
        }

        // Create dataset
        val dataSet = createLineDataSet(
            entries = entries,
            chartType = chartType,
            lineColor = lineColor,
            context = context,
            allDates = allDates,
            dateRange = dateRange,
            dailyTotals = dailyTotals
        )

        val lineData = LineData(dataSet)

        configureChart(
            chart = chart,
            lineData = lineData,
            xAxisLabels = xAxisLabels,
            context = context
        )
    }

    private fun createLineDataSet(
        entries: List<Entry>,
        chartType: String,
        lineColor: Int,
        context: Context,
        allDates: List<Long>,
        dateRange: ClosedRange<Long>,
        dailyTotals: Map<Long, Double>
    ): LineDataSet {
        return LineDataSet(entries, chartType.capitalize()).apply {
            color = lineColor
            lineWidth = 2f
            setCircleColor(lineColor)
            circleRadius = 3f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillDrawable = when (chartType) {
                "income" -> ContextCompat.getDrawable(context, R.drawable.gradient_income)
                else -> ContextCompat.getDrawable(context, R.drawable.gradient_expense)
            }
            setDrawValues(false)

            // Only show circles for dates with transactions or start/end dates
            setDrawCircles(true)
            setDrawCircleHole(false)
            setDrawCircles(true)
            setCircleRadius(3f)

            // Create a list of indices where we want to show circles
            val circleIndices = entries.indices.filter { index ->
                val date = allDates[index]
                isSameDay(date, dateRange.start) ||
                isSameDay(date, dateRange.endInclusive) ||
                dailyTotals[date] != 0.0
            }

            // Set circle colors for all points
            val circleColors = entries.indices.map { index ->
                if (index in circleIndices) lineColor else Color.TRANSPARENT
            }
            setCircleColors(circleColors)
        }
    }

    private fun configureChart(
        chart: LineChart,
        lineData: LineData,
        xAxisLabels: List<String>,
        context: Context
    ) {
        chart.apply {
            data = lineData

            // Configure x-axis
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                granularity = 1f
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                setLabelCount(xAxisLabels.count { it.isNotEmpty() }, true)
                axisMinimum = 0f
                axisMaximum = (xAxisLabels.size - 1).coerceAtLeast(0).toFloat()
                setAvoidFirstLastClipping(true)
                setDrawLabels(true)
                setDrawAxisLine(true)
                setCenterAxisLabels(false)
            }

            // Configure y-axis
            axisLeft.apply {
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                setDrawGridLines(true)
                gridLineWidth = 0.5f
                granularity = 100f
                setLabelCount(5, true)

                // Calculate min and max values with some padding
                val minY = lineData.yMin
                val maxY = lineData.yMax
                val padding = maxOf(Math.abs(maxY - minY) * 0.1f, 100f)

                // Set initial axis range
                axisMinimum = minOf(minY - padding, 0f)
                axisMaximum = maxY + padding
            }

            // Configure chart appearance
            setExtraOffsets(16f, 16f, 16f, 30f)
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false

            // Configure viewport
            setScaleEnabled(true)
            setVisibleXRange(0f, (xAxisLabels.size - 1).coerceAtLeast(0).toFloat())
            moveViewToX(0f)

            // Enable touch gestures
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // Animate
            animateXY(800, 800, Easing.EaseInOutCubic)

            // Refresh
            invalidate()
        }
    }

    //================================================================================
    // Pie Chart Methods
    //================================================================================
    fun setupPieChart(
        chart: PieChart,
        categories: List<Category>,
        transactions: List<Transaction>,
        isExpense: Boolean
    ) {
        val context = chart.context

        // Create entries for all categories, even those with no transactions
        val pieEntries = categories.map { category ->
            val categoryTotal = transactions
                .filter { it.category.id == category.id }
                .sumOf { it.amount.toDouble() }
                .toFloat()
            PieEntry(categoryTotal, category.name)
        }

        // Filter out categories with zero amount if you want to hide them
        val nonZeroPieEntries = pieEntries.filter { it.value > 0 }

        if (nonZeroPieEntries.isEmpty()) {
            chart.clear()
            chart.setNoDataText("No ${if (isExpense) "expenses" else "income"} data available")
            chart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
            chart.invalidate()
            return
        }

        val pieDataSet = PieDataSet(nonZeroPieEntries, if (isExpense) "Expenses" else "Income").apply {
            colors = categories
                .filter { cat -> nonZeroPieEntries.any { it.label == cat.name } }
                .map { ContextCompat.getColor(context, it.color) }
            valueTextSize = 14f
            valueTextColor = context.getThemeColor(R.attr.bb_background)
            valueTypeface = Typeface.DEFAULT_BOLD
            valueFormatter = PercentFormatter(chart)
        }

        val pieData = PieData(pieDataSet)

        chart.apply {
            data = pieData
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(context.getThemeColor(R.attr.bb_background))
            setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    //================================================================================
    // Utility Methods
    //================================================================================
    private fun generateDateRange(startDate: Long, endDate: Long): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (calendar.timeInMillis <= endDate) {
            dates.add(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}