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
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*
import androidx.core.util.Pair as UtilPair
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
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
        observeStates()
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
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnTimePeriod.setOnClickListener(){
                showDateRangePicker()
            }

            btnCategoryExpenseToggle.setOnClickListener {
                showCategoryExpenseToggle()
            }

            btnCategoryIncomeToggle.setOnClickListener {
                showCategoryIncomeToggle()
            }

            btnWalletArrow.setOnClickListener {
                spinnerWallet.performClick()
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
            }
        }
    }
    private fun updateUI() {
        when (val state = viewModel.transactionsState.value) {
            is GeneralReportsViewModel.TransactionState.Success -> {
                // Update charts with filtered transactions
                setupLineChart(state.transactions)
                setupPieChart(binding.btnCategoryExpenseToggle.background != null)

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
        // Get filtered transactions from ViewModel
        val filteredTransactions = when (val state = viewModel.transactionsState.value) {
            is GeneralReportsViewModel.TransactionState.Success -> state.transactions
            else -> emptyList()
        }

        val expenseTransactions = viewModel.getTransactionsByType("expense")
        val incomeTransactions = viewModel.getTransactionsByType("income")

        val totalExpense = expenseTransactions.sumOf { it.amount.toDouble() }
        val totalIncome = incomeTransactions.sumOf { it.amount.toDouble() }

        binding.apply {
            btnCategoryExpenseToggle.findViewById<TextView>(R.id.txtExpenseTotal).text =
                "-R ${String.format("%.2f", totalExpense)}"
            btnCategoryIncomeToggle.findViewById<TextView>(R.id.txtIncomeTotal).text =
                "R ${String.format("%.2f", totalIncome)}"
        }

        // Get unique category IDs from filtered transactions
        val categoryIdsWithTransactions = filteredTransactions
            .map { it.category.id }
            .toSet()

        // Filter categories to only those with transactions
        val expenseCategories = categories
            .filter {
                it.type.equals("expense", ignoreCase = true) &&
                        categoryIdsWithTransactions.contains(it.id)
            }
            .sortedBy { it.name }

        val incomeCategories = categories
            .filter {
                it.type.equals("income", ignoreCase = true) &&
                        categoryIdsWithTransactions.contains(it.id)
            }
            .sortedBy { it.name }

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val expenseItems = expenseCategories.map { category ->
            val categoryTransactions = filteredTransactions.filter { it.category.id == category.id }
            ReportListItems.ReportCategoryItem(
                category = category,
                transactionCount = categoryTransactions.size,
                amount = String.format("R %.2f", categoryTransactions.sumOf { it.amount.toDouble() }),
                relativeDate = categoryTransactions.maxByOrNull { it.date }?.let {
                    dateFormat.format(Date(it.date))
                } ?: "No date"
            )
        }

        val incomeItems = incomeCategories.map { category ->
            val categoryTransactions = filteredTransactions.filter { it.category.id == category.id }
            ReportListItems.ReportCategoryItem(
                category = category,
                transactionCount = categoryTransactions.size,
                amount = String.format("R %.2f", categoryTransactions.sumOf { it.amount.toDouble() }),
                relativeDate = categoryTransactions.maxByOrNull { it.date }?.let {
                    dateFormat.format(Date(it.date))
                } ?: "No date"
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
            highlightToggle(btnCategoryExpenseToggle, btnCategoryIncomeToggle)

            // Calculate and display total expenses
            val transactions = viewModel.getTransactionsByType("expense")
            val totalExpense = transactions.sumOf { it.amount.toDouble() }
            btnCategoryExpenseToggle.findViewById<TextView>(R.id.txtExpenseTotal).text =
                "${String.format("%.2f", totalExpense)}"
        }
    }

    private fun showCategoryIncomeToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.GONE
            recyclerViewIncomeCategory.visibility = View.VISIBLE
            setupPieChart(isExpense = false)
            highlightToggle(btnCategoryIncomeToggle, btnCategoryExpenseToggle)

            // Calculate and display total income
            val transactions = viewModel.getTransactionsByType("income")
            val totalIncome = transactions.sumOf { it.amount.toDouble() }
            btnCategoryIncomeToggle.findViewById<TextView>(R.id.txtIncomeTotal).text =
                "${String.format("%.2f", totalIncome)}"
        }
    }

    /**
     * Updates the visual state of toggle buttons.
     * 
     * @param selected The toggle button that is selected
     * @param unselected The toggle button that is not selected
     */
    private fun highlightToggle(selected: LinearLayout, unselected: LinearLayout) {
        selected.setBackgroundResource(R.drawable.toggle_selected)
        unselected.setBackgroundResource(android.R.color.transparent)
    }

    /**
     * Sets up the line chart showing income vs expense trends.
     * 
     * Chart Configuration:
     * 1. Data Processing:
     *    - Groups transactions by month
     *    - Calculates totals for income and expenses
     *    - Creates data entries for the chart
     * 
     * 2. Visual Setup:
     *    - Configures line styles and colors
     *    - Sets up axes and labels
     *    - Adds animations and interactions
     * 
     * 3. Performance:
     *    - Uses efficient data structures
     *    - Minimizes object creation
     *    - Optimizes drawing operations
     */

    private fun setupLineChart(transactions: List<Transaction>) {
        val lineChart: LineChart = binding.lineChart
        val context = lineChart.context

        // Get date range from ViewModel or use default (last 3 months)
        val calendar = Calendar.getInstance()
        val endDate = viewModel.dateRange.value?.endInclusive ?: calendar.timeInMillis

        calendar.timeInMillis = viewModel.dateRange.value?.start ?: run {
            calendar.add(Calendar.MONTH, -3)
            calendar.timeInMillis
        }
        val startDate = calendar.timeInMillis

        // Get all unique dates with transactions
        val transactionDates = transactions
            .filter { it.date in startDate..endDate }
            .map { transaction ->
                val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            .toSet()
            .sorted()

        if (transactionDates.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("No transaction data available")
            lineChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
            lineChart.invalidate()
            return
        }

        // Calculate running balance
        var runningBalance = 0.0
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val balanceEntries = mutableListOf<Entry>()
        val dates = mutableListOf<String>()

        // Add initial point if needed
        if (transactionDates.isNotEmpty()) {
            val firstDate = transactionDates.first()
            dates.add(dateFormat.format(Date(firstDate)))
            balanceEntries.add(Entry(0f, runningBalance.toFloat()))
        }

        // Process transactions in date order
        transactionDates.forEachIndexed { index, date ->
            // Get all transactions for this date
            val dailyTransactions = transactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis == date
            }

            // Update running balance
            dailyTransactions.forEach { transaction ->
                if (transaction.category.type.equals("income", ignoreCase = true)) {
                    runningBalance += transaction.amount.toDouble()
                } else {
                    runningBalance -= transaction.amount.toDouble()
                }
            }

            // Add entry for this date
            dates.add(dateFormat.format(Date(date)))
            balanceEntries.add(Entry((index + 1).toFloat(), runningBalance.toFloat()))
        }

        // Create dataset
        val dataSet = LineDataSet(balanceEntries, "Balance").apply {
            color = ContextCompat.getColor(context, R.color.button)
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(context, R.color.button))
            circleRadius = 3f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_wallet)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)

        lineChart.apply {
            clear()
            data = lineData

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dates)
                granularity = 1f
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                labelCount = minOf(7, dates.size)
                setCenterAxisLabels(false)
            }

            axisLeft.apply {
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                setDrawGridLines(true)
                gridLineWidth = 0.5f
                axisMinimum = 0f
            }

            setExtraOffsets(0f, 0f, 0f, 30f)
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(true)
            setPinchZoom(true)
            animateXY(1000, 1200, Easing.EaseInOutCubic)
            setVisibleXRangeMaximum(7f)
            moveViewToX(if (dates.size > 7) (dates.size - 7).toFloat() else 0f)
            invalidate()
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

        // Get filtered transactions first
        val transactions = if (isExpense) {
            viewModel.getTransactionsByType("expense")
        } else {
            viewModel.getTransactionsByType("income")
        }

        // Get all categories of the type
        val allCategories = if (isExpense) {
            viewModel.getCategoriesByType("expense")
        } else {
            viewModel.getCategoriesByType("income")
        }

        // Group transactions by category and calculate totals
        val categoryTotals = transactions
            .groupBy { it.category.id }
            .mapValues { (_, trans) -> trans.sumOf { it.amount.toDouble() } }

        // Filter out categories with no transactions
        val categoriesWithTransactions = allCategories.filter { category ->
            categoryTotals[category.id] != null
        }

        // Create pie entries only for categories with transactions
        val pieEntries = categoriesWithTransactions.map { category ->
            PieEntry(
                categoryTotals[category.id]?.toFloat() ?: 0f,
                category.name
            )
        }

        if (pieEntries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No data available")
            pieChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
            pieChart.invalidate()
            return
        }

        val pieDataSet = PieDataSet(pieEntries, if (isExpense) "Expenses" else "Income").apply {
            colors = categoriesWithTransactions.map { category ->
                ContextCompat.getColor(context, category.color)
            }
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
                            R.layout.spinner_item,
                            walletNames
                        ).apply {
                            setDropDownViewResource(R.layout.spinner_item)
                        }

                        binding.spinnerWallet.adapter = adapter

                        // Set the selection without triggering the listener
                        binding.spinnerWallet.onItemSelectedListener = null // Remove any existing listener first
                        binding.spinnerWallet.setSelection(0, false) // Select "All Wallets" by default

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

    // Add this function to update the button text
    private fun updateTimePeriodButtonText() {
        binding.btnTimePeriod.text = if (currentDateRange != null) {
            val startDate = Date(currentDateRange!!.first)
            val endDate = Date(currentDateRange!!.second)
            val dateFormat = DateFormat.getMediumDateFormat(requireContext())
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        } else {
            "Select Time Period"
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
        super.onDestroyView()
        _binding = null
    }
}
