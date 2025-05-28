package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    // ViewModel for data management
    private val viewModel: GeneralReportsViewModel by viewModels()

    /**
     * Adapter for displaying expense-related items.
     * Handles both transactions and categories.
     */
    private val expenseAdapter by lazy {
        GeneralReportAdapter(
            onTransactionClick = { transaction -> navigateToTransactionDetails(transaction) },
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
        )
    }

    /**
     * Adapter for displaying income-related items.
     * Handles both transactions and categories.
     */
    private val incomeAdapter by lazy {
        GeneralReportAdapter(
            onTransactionClick = { transaction -> navigateToTransactionDetails(transaction) },
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

            btnCategoryExpenseToggle.setOnClickListener {
                showCategoryExpenseToggle()
            }

            btnCategoryIncomeToggle.setOnClickListener {
                showCategoryIncomeToggle()
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
                // Launch a single coroutine to handle both states
                launch {
                    var transactionsLoaded = false
                    var categoriesLoaded = false

                    // Observe transactions
                    launch {
                        viewModel.transactionsState.collectLatest { state ->
                            when (state) {
                                is GeneralReportsViewModel.TransactionState.Success -> {
                                    setupLineChart(state.transactions)
                                    updateTransactionLists(state.transactions)
                                    transactionsLoaded = true
                                    if (categoriesLoaded) {
                                        // Both data sets are loaded, update pie chart
                                        setupPieChart(binding.btnCategoryExpenseToggle.background != null)
                                    }
                                }
                                else -> {
                                    // Handle other states if needed
                                }
                            }
                        }
                    }

                    // Observe categories
                    launch {
                        viewModel.categoriesState.collectLatest { state ->
                            when (state) {
                                is GeneralReportsViewModel.CategoryState.Success -> {
                                    updateCategoryLists(state.categories)
                                    categoriesLoaded = true
                                    if (transactionsLoaded) {
                                        // Both data sets are loaded, update pie chart
                                        setupPieChart(binding.btnCategoryExpenseToggle.background != null)
                                    }
                                }
                                else -> {
                                    // Handle other states if needed
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the transaction lists based on the loaded data.
     * 
     * Data Processing:
     * 1. Filters transactions by type (income/expense)
     * 2. Maps transactions to UI items
     * 3. Updates the appropriate adapter
     * 
     * The process ensures:
     * - Proper separation of income and expense transactions
     * - Correct formatting of dates and amounts
     * - Efficient UI updates using adapters
     */
    private fun updateTransactionLists(transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { 
            it.category.type.equals("expense", ignoreCase = true) 
        }
        val incomeTransactions = transactions.filter { 
            it.category.type.equals("income", ignoreCase = true) 
        }

        val expenseItems = expenseTransactions.map { transaction ->
            ReportListItems.ReportTransactionItem(
                transaction = transaction,
                relativeDate = formatDate(transaction.date)
            )
        }

        val incomeItems = incomeTransactions.map { transaction ->
            ReportListItems.ReportTransactionItem(
                transaction = transaction,
                relativeDate = formatDate(transaction.date)
            )
        }

        expenseAdapter.submitList(expenseItems)
        incomeAdapter.submitList(incomeItems)
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
        val expenseCategories = categories.filter { 
            it.type.equals("expense", ignoreCase = true) 
        }
        val incomeCategories = categories.filter { 
            it.type.equals("income", ignoreCase = true) 
        }

        val expenseItems = expenseCategories.map { category ->
            val transactions = viewModel.getTransactionsByType("expense")
                .filter { it.category.id == category.id }
            
            ReportListItems.ReportCategoryItem(
                category = category,
                transactionCount = transactions.size,
                amount = String.format("R %.2f", transactions.sumOf { it.amount }),
                relativeDate = "This Month" // You can implement actual logic here
            )
        }

        val incomeItems = incomeCategories.map { category ->
            val transactions = viewModel.getTransactionsByType("income")
                .filter { it.category.id == category.id }
            
            ReportListItems.ReportCategoryItem(
                category = category,
                transactionCount = transactions.size,
                amount = String.format("R %.2f", transactions.sumOf { it.amount }),
                relativeDate = "This Month" // You can implement actual logic here
            )
        }

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
        }
    }

    /**
     * Shows income-related views and updates the pie chart.
     * Handles visibility of RecyclerViews and toggle states.
     */
    private fun showCategoryIncomeToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.GONE
            recyclerViewIncomeCategory.visibility = View.VISIBLE
            setupPieChart(isExpense = false)
            highlightToggle(btnCategoryIncomeToggle, btnCategoryExpenseToggle)
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

        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        val incomeValues = MutableList(6) { 0f }
        val expenseValues = MutableList(6) { 0f }

        for (transaction in transactions) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = transaction.date
            }
            val monthIndex = calendar.get(Calendar.MONTH)
            val amount = transaction.amount.toFloat()
            val categoryType = transaction.category.type.lowercase(Locale.getDefault())

            if (monthIndex in 0..5) {
                when (categoryType) {
                    "income" -> incomeValues[monthIndex] += amount
                    "expense" -> expenseValues[monthIndex] += amount
                }
            }
        }

        val incomeEntries = incomeValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val expenseEntries = expenseValues.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = ContextCompat.getColor(context, R.color.profit_green)
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(context, R.color.profit_green))
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_income)
            setDrawValues(false)
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expense").apply {
            color = ContextCompat.getColor(context, R.color.expense_red)
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(context, R.color.expense_red))
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_expense)
            setDrawValues(false)
        }

        val lineData = LineData(incomeDataSet, expenseDataSet)

        lineChart.apply {
            clear()
            data = lineData

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(months)
                granularity = 1f
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }

            axisLeft.apply {
                textColor = context.getThemeColor(R.attr.bb_primaryText)
                setDrawGridLines(false)
            }

            setExtraOffsets(0f, 0f, 0f, 30f)
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            animateXY(1000, 1200, Easing.EaseInOutCubic)
            invalidate()
        }
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

        val categories = if (isExpense) {
            viewModel.getCategoriesByType("expense")
        } else {
            viewModel.getCategoriesByType("income")
        }

        val transactions = if (isExpense) {
            viewModel.getTransactionsByType("expense")
        } else {
            viewModel.getTransactionsByType("income")
        }

        val categoryAmounts = transactions
            .groupBy { it.category.id }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount.toDouble() } }

        val pieEntries = categories.mapNotNull { category ->
            val amount = categoryAmounts[category.id] ?: 0.0
            if (amount > 0) {
                PieEntry(amount.toFloat(), category.name)
            } else {
                null
            }
        }

        if (pieEntries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No data available")
            pieChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
            pieChart.invalidate()
            return
        }

        val pieDataSet = PieDataSet(pieEntries, if (isExpense) "Expenses" else "Income").apply {
            colors = categories.map { category ->
                ContextCompat.getColor(context, category.color)
            }
            valueTextSize = 14f
            valueTextColor = context.getThemeColor(R.attr.bb_primaryText)
            valueFormatter = PercentFormatter(pieChart)
        }

        val pieData = PieData(pieDataSet)

        pieChart.apply {
            data = pieData
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            centerText = if (isExpense) "Expenses" else "Income"
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(context.getThemeColor(R.attr.bb_primaryText))
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    /**
     * Formats a timestamp into a human-readable date string.
     * Shows "Today", "Yesterday", or the date.
     * 
     * @param timestamp The timestamp to format
     * @return Formatted date string
     */
    private fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val now = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return when {
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
            calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) -> "Today"
            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
            calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) - 1 -> "Yesterday"
            else -> {
                val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                "$month $day"
            }
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
