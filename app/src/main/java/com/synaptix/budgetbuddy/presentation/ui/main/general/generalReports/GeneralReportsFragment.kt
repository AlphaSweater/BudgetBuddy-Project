package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import android.text.format.DateFormat
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*
import androidx.core.util.Pair as UtilPair
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fragment for displaying financial reports and analytics.
 * 
 * This fragment is responsible for:
 * 1. Displaying transaction and category data
 * 2. Showing visual representations (charts) of the data
 * 3. Handling user interactions
 * 4. Managing UI state
 * 
 * The fragment uses:
 * - ViewBinding for view access
 * - ViewModel for data management
 * - Coroutines for asynchronous operations
 * - MPAndroidChart for data visualization
 * 
 * Data Flow and State Management:
 * 1. ViewModel StateFlow:
 *    - The ViewModel exposes StateFlow objects for each data type
 *    - These flows emit updates whenever the data changes
 *    - The Fragment collects these flows to update the UI
 * 
 * 2. Coroutine Lifecycle:
 *    - viewLifecycleOwner.lifecycleScope: Coroutine scope tied to Fragment lifecycle
 *    - repeatOnLifecycle: Ensures coroutines are cancelled when Fragment is stopped
 *    - collectLatest: Collects the latest value from a Flow
 * 
 * 3. State Observation:
 *    - Each data type (transactions, categories) has its own observer
 *    - Observers run in parallel using separate coroutines
 *    - UI is updated based on the current state
 * 
 * 4. Error Handling:
 *    - Each state type (Loading, Success, Error, Empty) is handled appropriately
 *    - Loading states show progress indicators
 *    - Error states show empty states with messages
 *    - Success states update the UI with data
 */
@AndroidEntryPoint
class GeneralReportsFragment : Fragment() {

    // ViewBinding for safe view access
    private var _binding: FragmentGeneralReportsBinding? = null
    private val binding get() = _binding!!

    private var currentDateRange: Pair<Long, Long>? = null

    // ViewModel for data management
    private val viewModel: GeneralReportsViewModel by viewModels()

    private lateinit var lineChartExpense: LineChart
    private lateinit var lineChartIncome: LineChart

    private var expenseGoalJob: Job? = null

    /**
     * Adapter for displaying expense-related items.
     * Handles both transactions and categories.
     */
    private val expenseAdapter by lazy {
        GeneralReportAdapter(
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
        )
    }

    /**
     * Adapter for displaying income-related items.
     * Handles both transactions and categories.
     */
    private val incomeAdapter by lazy {
        GeneralReportAdapter(
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupOnClickListeners()
        setupWalletDropdown()
        setupPieChart(true)
        observeStates()

        // Set initial toggle state
        binding.btnChartExpenseToggle.setBackgroundResource(R.drawable.toggle_selected)
        binding.btnChartIncomeToggle.setBackgroundResource(android.R.color.transparent)
        // Initialize views
        lineChartExpense = binding.lineChartExpense
        lineChartIncome = binding.lineChartIncome

        // Show expense chart by default
        lineChartExpense.visibility = View.VISIBLE
        lineChartIncome.visibility = View.GONE
    }

    /**
     * Sets up the RecyclerViews for displaying transactions and categories.
     * Uses LinearLayoutManager for vertical scrolling.
     */
    private fun setupRecyclerViews() {
        binding.apply {
            recyclerViewExpenseCategory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = expenseAdapter
            }

            recyclerViewIncomeCategory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = incomeAdapter
            }
        }
    }

    /**
     * Sets up click listeners for UI elements.
     * Handles navigation and toggle actions.
     */
    private fun setupOnClickListeners() {
        binding.apply {
            // Back button
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            // Date selection
            btnSelectDate.setOnClickListener {
                showDateRangePicker()
            }

            btnClearDate.setOnClickListener {
                currentDateRange = null
                updateTimePeriodButtonText()
                viewModel.clearDateRange()
                tvDateRange.text = "Select Date"
            }

            // Chart toggles
            btnChartExpenseToggle.setOnClickListener {
                showChartExpense()
            }

            btnChartIncomeToggle.setOnClickListener {
                showChartIncome()
            }

            // Category toggles
            btnCatExpenseToggle.setOnClickListener {
                showCategoryExpenseToggle()
            }

            btnCatIncomeToggle.setOnClickListener {
                showCategoryIncomeToggle()
            }

            // Label toggles
            btnLabelExpenseToggle.setOnClickListener {
                showLabelExpenseToggle()
            }

            btnLabelIncomeToggle.setOnClickListener {
                showLabelIncomeToggle()
            }
        }
    }

    /**
     * Observes ViewModel states using coroutines.
     * 
     * This method:
     * 1. Creates a coroutine scope tied to the Fragment's lifecycle
     * 2. Uses repeatOnLifecycle to handle lifecycle changes
     * 3. Launches separate coroutines for each data type
     * 
     * The coroutines will:
     * - Start when the Fragment is started
     * - Be cancelled when the Fragment is stopped
     * - Restart when the Fragment is started again
     * 
     * This ensures efficient resource usage and prevents memory leaks.
     */
    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Load data when the fragment starts
                viewModel.loadData()

                // Show expense toggle by default when fragment first loads
                showCategoryExpenseToggle()

                launch {
                    // Observe wallet selection changes
                    viewModel.selectedWallet.collect { selectedWallet ->
                        Log.d("Filtering", "Wallet changed: ${selectedWallet?.name}")
                        updateUI()
                    }
                }

                launch {
                    // Observe date range changes
                    viewModel.dateRange.collect { dateRange ->
                        Log.d("Filtering", "Date range changed: $dateRange")
                        updateUI()
                    }
                }

                launch {
                    // Observe transactions
                    viewModel.transactionsState.collect { state ->
                        when (state) {
                            is GeneralReportsViewModel.TransactionState.Success -> {
                                Log.d("Filtering", "Transactions updated: ${state.transactions.size} items")
                                updateUI()
                            }
                            is GeneralReportsViewModel.TransactionState.Loading -> {
                                // Show loading state if needed
                                Log.d("Filtering", "Loading transactions...")
                            }
                            is GeneralReportsViewModel.TransactionState.Error -> {
                                Log.e("Filtering", "Error loading transactions: ${state.message}")
                                // Show error state if needed
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    // Observe categories
                    viewModel.categoriesState.collect { state ->
                        when (state) {
                            is GeneralReportsViewModel.CategoryState.Success -> {
                                Log.d("Filtering", "Categories updated: ${state.categories.size} items")
                                updateCategoryLists(state.categories)
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.expenseGoal.collect { goals ->
                        goals?.let { (minGoal, maxGoal) ->
                            Log.d("ExpenseGoal", "Received goals - Min: $minGoal, Max: $maxGoal")
                            // Only update if the expense chart is visible
                            if (lineChartExpense.visibility == View.VISIBLE) {
                                updateExpenseGoalLines(minGoal, maxGoal)
                            }
                        } ?: run {
                            // Clear the lines if goals are null
                            lineChartExpense.axisLeft.removeAllLimitLines()
                            lineChartExpense.invalidate()
                        }
                    }
                }
            }
        }
    }
    private fun updateUI() {
        when (val state = viewModel.transactionsState.value) {
            is GeneralReportsViewModel.TransactionState.Success -> {
                // Update charts with filtered transactions
                val expenseTransactions = viewModel.getTransactionsByType("expense")
                val incomeTransactions = viewModel.getTransactionsByType("income")

                // Update both charts
                setupLineChart(lineChartExpense, expenseTransactions, "expense")
                setupLineChart(lineChartIncome, incomeTransactions, "income")

                // Update pie chart
                setupPieChart(binding.btnCatExpenseToggle.background != null)

                // Update category lists if we have categories loaded
                (viewModel.categoriesState.value as? GeneralReportsViewModel.CategoryState.Success)?.let { categoryState ->
                    updateCategoryLists(categoryState.categories)
                }
            }
            is GeneralReportsViewModel.TransactionState.Loading -> {
                // Show loading state if needed
            }
            is GeneralReportsViewModel.TransactionState.Error -> {
                // Show error state if needed
            }
            is GeneralReportsViewModel.TransactionState.Empty -> {
                // Show empty state if needed
            }
        }
    }

    /**
     * Updates the category lists based on the loaded data.
     * 
     * Data Processing:
     * 1. Filters categories by type (income/expense)
     * 2. Calculates transaction counts and amounts
     * 3. Maps categories to UI items
     * 4. Updates the appropriate adapter
     * 
     * The process ensures:
     * - Proper separation of income and expense categories
     * - Accurate calculation of transaction statistics
     * - Efficient UI updates using adapters
     */
    private fun updateCategoryLists(categories: List<Category>) {
        val filteredTransactions = when (val state = viewModel.transactionsState.value) {
            is GeneralReportsViewModel.TransactionState.Success -> state.transactions
            else -> emptyList()
        }

        val expenseTransactions = viewModel.getTransactionsByType("expense")
        val incomeTransactions = viewModel.getTransactionsByType("income")

        val totalExpense = expenseTransactions.sumOf { it.amount.toDouble() }
        val totalIncome = incomeTransactions.sumOf { it.amount.toDouble() }

        binding.apply {
            btnCatExpenseToggle.findViewById<TextView>(R.id.txtExpenseTotal).text =
                "-R ${String.format("%.2f", totalExpense)}"
            btnCatIncomeToggle.findViewById<TextView>(R.id.txtIncomeTotal).text =
                "R ${String.format("%.2f", totalIncome)}"

            // Update chart toggle amounts
            txtChartExpenseTotal.text = "-R ${String.format("%.2f", totalExpense)}"
            txtChartIncomeTotal.text = "R ${String.format("%.2f", totalIncome)}"
        }

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Get all expense categories, not just those with transactions
        val expenseCategories = categories
            .filter { it.type.equals("expense", ignoreCase = true) }
            .sortedBy { it.name }

        // Get all income categories, not just those with transactions
        val incomeCategories = categories
            .filter { it.type.equals("income", ignoreCase = true) }
            .sortedBy { it.name }

        // Map all expense categories, including those with no transactions
        val expenseItems = expenseCategories.map { category ->
            val categoryTransactions = filteredTransactions.filter { it.category.id == category.id }
            ReportListItems.ReportCategoryItem(
                category = category,
                transactionCount = categoryTransactions.size,
                amount = String.format("R %.2f", categoryTransactions.sumOf { it.amount.toDouble() }),
                relativeDate = categoryTransactions.maxByOrNull { it.date }?.let {
                    dateFormat.format(Date(it.date))
                } ?: "No transactions"
            )
        }

        // Map all income categories, including those with no transactions
        val incomeItems = incomeCategories.map { category ->
            val categoryTransactions = filteredTransactions.filter { it.category.id == category.id }
            ReportListItems.ReportCategoryItem(
                category = category,
                transactionCount = categoryTransactions.size,
                amount = String.format("R %.2f", categoryTransactions.sumOf { it.amount.toDouble() }),
                relativeDate = categoryTransactions.maxByOrNull { it.date }?.let {
                    dateFormat.format(Date(it.date))
                } ?: "No transactions"
            )
        }

        updateTotalBalance(filteredTransactions)
        expenseAdapter.submitList(expenseItems)
        incomeAdapter.submitList(incomeItems)
    }

    /**
     * Shows expense-related views and updates the pie chart.
     * Handles visibility of RecyclerViews and toggle states.
     */
    private fun showCategoryExpenseToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.VISIBLE
            recyclerViewIncomeCategory.visibility = View.GONE
            setupPieChart(isExpense = true)
            highlightToggle(btnCatExpenseToggle, btnCatIncomeToggle)

            // Show 0.00 if no expenses
            val transactions = viewModel.getTransactionsByType("expense")
            val totalExpense = transactions.sumOf { it.amount.toDouble() }
            txtExpenseTotal.text =
                if (totalExpense > 0) "-R ${String.format("%.2f", totalExpense)}"
                else "R 0.00"
        }
    }

    private fun showCategoryIncomeToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.GONE
            recyclerViewIncomeCategory.visibility = View.VISIBLE
            setupPieChart(isExpense = false)
            highlightToggle(btnCatIncomeToggle, btnCatExpenseToggle)

            // Show 0.00 if no income
            val transactions = viewModel.getTransactionsByType("income")
            val totalIncome = transactions.sumOf { it.amount.toDouble() }
            txtIncomeTotal.text =
                if (totalIncome > 0) "R ${String.format("%.2f", totalIncome)}"
                else "R 0.00"
        }
    }

    private fun highlightToggle(selected: LinearLayout, unselected: LinearLayout) {
        selected.setBackgroundResource(R.drawable.toggle_selected)
        unselected.setBackgroundResource(android.R.color.transparent)
    }

    private fun showChartExpense() {
        binding.apply {
            lineChartExpense.visibility = View.VISIBLE
            lineChartIncome.visibility = View.GONE
            highlightChartToggle(btnChartExpenseToggle, btnChartIncomeToggle)
            updateChartData()

            // Update amounts when toggled
            val transactions = viewModel.getTransactionsByType("expense")
            val totalExpense = transactions.sumOf { it.amount.toDouble() }
            txtChartExpenseTotal.text =
                if (totalExpense > 0) "-R ${String.format("%.2f", totalExpense)}"
                else "R 0.00"
        }
    }

    private fun showChartIncome() {
        binding.apply {
            lineChartExpense.visibility = View.GONE
            lineChartIncome.visibility = View.VISIBLE
            highlightChartToggle(btnChartIncomeToggle, btnChartExpenseToggle)
            updateChartData()

            // Update amounts when toggled
            val transactions = viewModel.getTransactionsByType("income")
            val totalIncome = transactions.sumOf { it.amount.toDouble() }
            txtChartIncomeTotal.text =
                if (totalIncome > 0) "R ${String.format("%.2f", totalIncome)}"
                else "R 0.00"
        }
    }

    private fun highlightChartToggle(selected: View, unselected: View) {
        selected.setBackgroundResource(R.drawable.toggle_selected)
        unselected.setBackgroundResource(android.R.color.transparent)
    }

    // Update the chart data based on the visible chart
    private fun updateChartData() {
        when {
            lineChartExpense.visibility == View.VISIBLE -> {
                val expenseTransactions = viewModel.getTransactionsByType("expense")
                setupLineChart(lineChartExpense, expenseTransactions, "expense")
            }
            lineChartIncome.visibility == View.VISIBLE -> {
                val incomeTransactions = viewModel.getTransactionsByType("income")
                setupLineChart(lineChartIncome, incomeTransactions, "income")
            }
        }
    }

    private fun setupLineChart(chart: LineChart, transactions: List<Transaction>, chartType: String) {
        val context = chart.context
        chart.clear()
        chart.setNoDataText("No ${chartType} data available")
        chart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))

        if (transactions.isEmpty()) {
            chart.invalidate()
            return
        }

        // Sort transactions by date
        val sortedTransactions = transactions.sortedBy { it.date }

        // Set line color based on chart type
        var lineColor = when (chartType) {
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

        // Create a list of all days in the date range
        val dateRange = if (sortedTransactions.size > 1) {
            val startDate = sortedTransactions.first().date
            val endDate = sortedTransactions.last().date
            generateDateRange(startDate, endDate)
        } else {
            // If there's only one transaction, show it with a point before and after
            val date = sortedTransactions.first().date
            listOf(
                date - 86400000, // 1 day before
                date,
                date + 86400000  // 1 day after
            )
        }

        // Prepare data entries and x-axis labels
        val entries = mutableListOf<Entry>()
        val xAxisLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Track running totals for each day
        val dailyTotals = mutableMapOf<Long, Double>()

        // Initialize all days with zero
        dateRange.forEach { date ->
            dailyTotals[date] = 0.0
        }

        // Calculate daily totals
        dailyTransactions.forEach { (date, dayTransactions) ->
            val dailyTotal = dayTransactions.sumOf { it.amount.toDouble() }
            dailyTotals[date] = dailyTotal
        }

        // Calculate cumulative totals
        var runningTotal = 0.0
        dateRange.sorted().forEachIndexed { index, date ->
            runningTotal += dailyTotals[date] ?: 0.0
            entries.add(Entry(index.toFloat(), runningTotal.toFloat()))
            xAxisLabels.add(dateFormat.format(Date(date)))
        }

        // Create dataset
        val dataSet = LineDataSet(entries, chartType.capitalize()).apply {
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
        }

        val lineData = LineData(dataSet)

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
                setLabelCount(minOf(7, xAxisLabels.size), true)
                axisMinimum = 0f
                axisMaximum = (xAxisLabels.size - 1).coerceAtLeast(0).toFloat()
                setAvoidFirstLastClipping(true)
            }

            // Configure y-axis
            axisLeft.apply {
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                setDrawGridLines(true)
                gridLineWidth = 0.5f
                granularity = 100f
                setLabelCount(5, true)

                // Calculate min and max values with some padding
                val minY = entries.minByOrNull { it.y }?.y ?: 0f
                val maxY = entries.maxByOrNull { it.y }?.y ?: 0f
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

        // Handle expense goal lines if this is the expense chart
        if (chartType == "expense") {
            // Cancel any existing job to prevent leaks
            expenseGoalJob?.cancel()

            expenseGoalJob = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.expenseGoal.collect { goals ->
                    goals?.let { (minGoal, maxGoal) ->
                        // Remove existing limit lines
                        chart.axisLeft.removeAllLimitLines()

                        // Only add goal lines if they have valid values
                        if (minGoal > 0) {
                            val minLine = LimitLine(minGoal.toFloat(), "Min Goal").apply {
                                lineColor = ContextCompat.getColor(context, R.color.min_graph)
                                lineWidth = 1f
                                enableDashedLine(10f, 10f, 0f)
                                textColor = ContextCompat.getColor(context, R.color.min_graph)
                                textSize = 10f
                            }
                            chart.axisLeft.addLimitLine(minLine)
                        }

                        if (maxGoal > 0) {
                            val maxLine = LimitLine(maxGoal.toFloat(), "Max Goal").apply {
                                lineColor = ContextCompat.getColor(context, R.color.max_graph)
                                lineWidth = 1f
                                enableDashedLine(10f, 10f, 0f)
                                textColor = ContextCompat.getColor(context, R.color.max_graph)
                                textSize = 10f
                            }
                            chart.axisLeft.addLimitLine(maxLine)
                        }

                        // Get the current chart data
                        val entries = (chart.data?.dataSets?.firstOrNull() as? LineDataSet)?.values ?: return@collect

                        // Adjust y-axis to include the goal lines and data
                        val minY = entries.minByOrNull { it.y }?.y ?: 0f
                        val maxY = entries.maxByOrNull { it.y }?.y ?: 0f
                        val padding = maxOf(Math.abs(maxY - minY) * 0.1f, 100f)

                        // Include goal lines in axis range if they exist
                        val minAxis = minOf(
                            minY - padding,
                            if (minGoal > 0) minGoal.toFloat() - padding else minY - padding,
                            0f
                        )
                        val maxAxis = maxOf(
                            maxY + padding,
                            if (maxGoal > 0) maxGoal.toFloat() + padding else maxY + padding
                        )

                        // Apply the new axis range
                        chart.axisLeft.axisMinimum = minAxis
                        chart.axisLeft.axisMaximum = maxAxis

                        val (minGoal, maxGoal) = viewModel.expenseGoal.value ?: (0.0 to 0.0)
                        updateExpenseGoalLines(minGoal, maxGoal)
                        // Make sure to refresh the chart
                        chart.invalidate()
                    }
                }
            }
        }
    }

    private fun updateTotalBalance(transactions: List<Transaction>) {
        val totalBalance = transactions.sumOf { it.amount }
        binding.tvCurrencyTotal.text = String.format("R %.2f", totalBalance)
    }

    /**
     * Sets up the pie chart showing category breakdown.
     * 
     * Chart Configuration:
     * 1. Data Processing:
     *    - Filters transactions by type
     *    - Groups transactions by category
     *    - Calculates percentages
     * 
     * 2. Visual Setup:
     *    - Configures slice colors and labels
     *    - Sets up center text and hole
     *    - Adds animations and interactions
     * 
     * 3. Performance:
     *    - Uses efficient data structures
     *    - Minimizes object creation
     *    - Optimizes drawing operations
     */
    private fun setupPieChart(isExpense: Boolean) {
        val context = binding.root.context
        val pieChart: PieChart = binding.pieChart

        // Get all categories of the type first
        val allCategories = if (isExpense) {
            viewModel.getCategoriesByType("expense")
        } else {
            viewModel.getCategoriesByType("income")
        }

        // Then get filtered transactions
        val transactions = if (isExpense) {
            viewModel.getTransactionsByType("expense")
        } else {
            viewModel.getTransactionsByType("income")
        }

        // Create entries for all categories, even those with no transactions
        val pieEntries = allCategories.map { category ->
            val categoryTotal = transactions
                .filter { it.category.id == category.id }
                .sumOf { it.amount.toDouble() }
                .toFloat()
            PieEntry(categoryTotal, category.name)
        }

        // Filter out categories with zero amount if you want to hide them
        val nonZeroPieEntries = pieEntries.filter { it.value > 0 }

        if (nonZeroPieEntries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No ${if (isExpense) "expenses" else "income"} data available")
            pieChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
            pieChart.invalidate()
            return
        }

        val pieDataSet = PieDataSet(nonZeroPieEntries, if (isExpense) "Expenses" else "Income").apply {
            colors = allCategories
                .filter { cat -> nonZeroPieEntries.any { it.label == cat.name } }
                .map { ContextCompat.getColor(context, it.color) }
            valueTextSize = 14f
            valueTextColor = context.getThemeColor(R.attr.bb_background)
            valueTypeface = Typeface.DEFAULT_BOLD
            valueFormatter = PercentFormatter(pieChart)
        }

        val pieData = PieData(pieDataSet)

        pieChart.apply {
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

    private fun setupWalletDropdown() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.walletState.collect { wallets ->
                    Log.d("WalletDropdown", "walletState emitted with wallets: ${wallets.map { it.name }}")

                    if (wallets.isNotEmpty()) {
                        // Create a list with "All Wallets" as the first item
                        val walletNames = listOf("All Wallets") + wallets.map { it.name }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.item_dropdown_item,
                            walletNames
                        ).apply {
                            setDropDownViewResource(R.layout.item_dropdown_item)
                        }

                        binding.spinnerWallet.adapter = adapter

                        // Set up the item selected listener
                        binding.spinnerWallet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedWallet = if (position == 0) {
                                    null // "All Wallets" selected
                                } else {
                                    wallets[position - 1] // Adjust index since we added "All Wallets" at position 0
                                }
                                viewModel.selectWallet(selectedWallet)
                                Log.d("WalletDropdown", "Selected wallet: ${selectedWallet?.name ?: "All Wallets"}")
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                viewModel.selectWallet(null)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Default to last 30 days
        val defaultEndDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val defaultStartDate = calendar.timeInMillis

        // Create and show the date range picker dialog
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setSelection(
                UtilPair(
                    defaultStartDate,
                    defaultEndDate
                )
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first ?: return@addOnPositiveButtonClickListener
            val endDate = selection.second ?: return@addOnPositiveButtonClickListener
            currentDateRange = startDate to endDate
            updateTimePeriodButtonText()
            viewModel.setDateRange(startDate, endDate)
        }

        dateRangePicker.addOnNegativeButtonClickListener {
            currentDateRange = null
            updateTimePeriodButtonText()
            viewModel.clearDateRange()
        }

        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

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

    // Add this function to update the button text
    private fun updateTimePeriodButtonText() {
        binding.tvDateRange.text = if (currentDateRange != null) {
            val startDate = Date(currentDateRange!!.first)
            val endDate = Date(currentDateRange!!.second)
            val dateFormat = DateFormat.getMediumDateFormat(requireContext())
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        } else {
            "Select Date"
        }
    }

    private fun updateExpenseGoalLines(minGoal: Double, maxGoal: Double) {
        Log.d("ExpenseGoalLines", "Updating goal lines - Min: $minGoal, Max: $maxGoal")

        // Only update if we're showing the expense chart
        if (lineChartExpense.visibility != View.VISIBLE) {
            Log.d("ExpenseGoalLines", "Expense chart not visible, skipping update")
            return
        }

        // Remove existing limit lines
        lineChartExpense.axisLeft.removeAllLimitLines()

        // Get the current chart data
        val entries = (lineChartExpense.data?.dataSets?.firstOrNull() as? LineDataSet)?.values ?: run {
            Log.d("ExpenseGoalLines", "No chart data available")
            lineChartExpense.invalidate()
            return
        }

        // Calculate min and max Y values from data
        val minY = entries.minByOrNull { it.y }?.y ?: 0f
        val maxY = entries.maxByOrNull { it.y }?.y ?: 0f

        Log.d("ExpenseGoalLines", "Chart data range - MinY: $minY, MaxY: $maxY")

        // Calculate padding (20% of the range or 100, whichever is larger)
        val dataRange = maxY - minY
        val padding = maxOf(dataRange * 0.2f, 100f)

        // Calculate the minimum and maximum values to show, including goal lines
        val valuesToConsider = mutableListOf<Float>().apply {
            add(minY)
            add(maxY)
            if (minGoal > 0) add(minGoal.toFloat())
            if (maxGoal > 0) add(maxGoal.toFloat())
        }

        val minValue = valuesToConsider.minOrNull() ?: 0f
        val maxValue = valuesToConsider.maxOrNull() ?: 0f

        // Calculate extra space needed for the max goal label (50% more padding at the top)
        val extraTopSpace = padding * 0.5f

        // Apply padding to the range with extra space at the top
        val minAxis = minValue - padding
        val maxAxis = maxValue + padding + extraTopSpace

        // Store limit lines to add them in the correct order
        val limitLines = mutableListOf<LimitLine>()

        // Add min goal line if valid
        if (minGoal > 0) {
            val minLine = LimitLine(minGoal.toFloat(), "Min Goal").apply {
                lineColor = ContextCompat.getColor(requireContext(), R.color.min_graph)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                textColor = ContextCompat.getColor(requireContext(), R.color.min_graph)
                textSize = 12f
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                xOffset = 12f
                yOffset = 0f
            }
            limitLines.add(minLine)
        }

        // Add max goal line if valid
        if (maxGoal > 0) {
            val maxLine = LimitLine(maxGoal.toFloat(), "Max Goal").apply {
                lineColor = ContextCompat.getColor(requireContext(), R.color.max_graph)
                lineWidth = 2f
                enableDashedLine(10f, 10f, 0f)
                textColor = ContextCompat.getColor(requireContext(), R.color.max_graph)
                textSize = 12f
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                xOffset = 12f
                yOffset = -20f  // Negative offset to move the label down
            }
            limitLines.add(maxLine)
        }

        // Add limit lines in the correct order (min first, then max)
        limitLines.forEach { lineChartExpense.axisLeft.addLimitLine(it) }

        // Set chart offsets to ensure labels are visible
        // Parameters: left, top, right, bottom
        lineChartExpense.setExtraOffsets(16f, 40f, 32f, 30f)

        Log.d("ExpenseGoalLines", "Setting axis range - Min: $minAxis, Max: $maxAxis")

        // Apply the new axis range
        lineChartExpense.axisLeft.apply {
            axisMinimum = minAxis
            axisMaximum = maxAxis
            setDrawLimitLinesBehindData(true)
            setDrawLabels(true)
            setDrawAxisLine(true)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            xOffset = 12f
            setLabelCount(5, true)
            // Ensure the axis is recalculated
            setStartAtZero(false)
        }

        // Force a complete redraw with the new settings
        lineChartExpense.setVisibleXRangeMaximum(entries.size.toFloat())
        lineChartExpense.moveViewToX(0f)
        lineChartExpense.setExtraTopOffset(40f)  // Additional top offset
        lineChartExpense.setExtraBottomOffset(20f)
        lineChartExpense.setExtraLeftOffset(16f)
        lineChartExpense.setExtraRightOffset(32f)
        lineChartExpense.invalidate()
        lineChartExpense.requestLayout()

        Log.d("ExpenseGoalLines", "Chart updated with goal lines")
    }

    private fun showLabelExpenseToggle() {
        binding.apply {
            // TODO: Implement label expense view
            highlightToggle(btnLabelExpenseToggle, btnLabelIncomeToggle)
        }
    }

    private fun showLabelIncomeToggle() {
        binding.apply {
            // TODO: Implement label income view
            highlightToggle(btnLabelIncomeToggle, btnLabelExpenseToggle)
        }
    }

    /**
     * Navigates to transaction details screen.
     * To be implemented when the details screen is ready.
     */
    private fun navigateToTransactionDetails(transaction: Transaction) {
        // TODO: Implement navigation to transaction details
    }

    /**
     * Navigates to category details screen.
     * To be implemented when the details screen is ready.
     */
    private fun navigateToCategoryDetails(category: Category) {
        // TODO: Implement navigation to category details
    }

    override fun onDestroyView() {
        expenseGoalJob?.cancel()
        expenseGoalJob = null
        _binding = null
        super.onDestroyView()
    }
}
