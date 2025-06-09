package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.core.util.DateUtil
import com.synaptix.budgetbuddy.core.util.PrivacyUtil
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import com.synaptix.budgetbuddy.presentation.ui.main.general.GeneralViewModel
import com.synaptix.budgetbuddy.core.util.GraphUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    //================================================================================
    // Properties
    //================================================================================
    private var isBalanceVisible = true
    private var _binding: FragmentGeneralReportsBinding? = null
    private val binding get() = _binding!!
    private var currentDateRange: Pair<Long, Long>? = null
    private val viewModel: GeneralViewModel by activityViewModels()
    private lateinit var lineChartExpense: LineChart
    private lateinit var lineChartIncome: LineChart
    private var expenseGoalJob: Job? = null

    // Adapters for categories
    private val categoryExpenseAdapter by lazy {
        GeneralReportAdapter(
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
        )
    }

    private val categoryIncomeAdapter by lazy {
        GeneralReportAdapter(
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
        )
    }

    // Adapters for labels
    private val labelExpenseAdapter by lazy {
        GeneralReportAdapter(
            onLabelClick = { label -> navigateToLabelDetails(label) }
        )
    }

    private val labelIncomeAdapter by lazy {
        GeneralReportAdapter(
            onLabelClick = { label -> navigateToLabelDetails(label) }
        )
    }

    //================================================================================
    // Lifecycle Methods
    //================================================================================
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
        
        // Initialize chart views
        lineChartExpense = binding.lineChartExpense
        lineChartIncome = binding.lineChartIncome

        // Set initial visibility
        lineChartExpense.visibility = View.VISIBLE
        lineChartIncome.visibility = View.GONE

        // Set initial toggle states
        binding.btnChartExpenseToggle.setBackgroundResource(R.drawable.toggle_selected)
        binding.btnChartIncomeToggle.setBackgroundResource(android.R.color.transparent)

        // Setup all views and listeners
        setupViews()
        
        // Start observing states
        observeViewModel()
    }

    override fun onDestroyView() {
        expenseGoalJob?.cancel()
        expenseGoalJob = null
        _binding = null
        super.onDestroyView()
    }

    //================================================================================
    // Setup Methods
    //================================================================================
    private fun setupViews() {
        setupRecyclerViews()
        setupOnClickListeners()
        setupWalletDropdown()
        setupPieChart(true)
        setupViewSwitcher()
    }

    private fun setupRecyclerViews() {
        binding.apply {
            // Category recycler views
            recyclerViewExpenseCategory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryExpenseAdapter
            }

            recyclerViewIncomeCategory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryIncomeAdapter
            }

            // Label recycler views
            recyclerViewLabelExpense.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = labelExpenseAdapter
            }

            recyclerViewLabelIncome.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = labelIncomeAdapter
            }
        }
    }

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
                updateDateRangeText()
                viewModel.clearDateRange()
            }

            // Chart toggles
            btnChartExpenseToggle.setOnClickListener {
                showChartExpense()
                showCategoryExpenseToggle()
                showLabelExpenseToggle()
            }

            btnChartIncomeToggle.setOnClickListener {
                showChartIncome()
                showCategoryIncomeToggle()
                showLabelIncomeToggle()
            }

            // Category toggles
            btnCatExpenseToggle.setOnClickListener {
                showCategoryExpenseToggle()
                showChartExpense()
                showLabelExpenseToggle()
            }

            btnCatIncomeToggle.setOnClickListener {
                showCategoryIncomeToggle()
                showChartIncome()
                showLabelIncomeToggle()
            }

            // Label toggles
            btnLabelExpenseToggle.setOnClickListener {
                showLabelExpenseToggle()
                showChartExpense()
                showCategoryExpenseToggle()
            }

            btnLabelIncomeToggle.setOnClickListener {
                showLabelIncomeToggle()
                showChartIncome()
                showCategoryIncomeToggle()
            }

            btnViewEye.setOnClickListener {
                isBalanceVisible = PrivacyUtil.toggleBalanceVisibility(
                    isVisible = isBalanceVisible,
                    balanceView = textViewCurrencyTotal,
                    eyeIcon = imageViewEye,
                    balance = viewModel.totalBalance.value
                )
            }
        }
    }

    private fun setupWalletDropdown() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.walletState.collectLatest { walletState ->
                    when (walletState) {
                        is GeneralViewModel.WalletState.Success -> {
                            val wallets = walletState.wallets
                            if (wallets.isNotEmpty()) {
                                // Create a list with "All Wallets" as the first item
                                val walletNames = listOf("All Wallets") + wallets.map { it.name }

                                val adapter = ArrayAdapter(
                                    requireContext(),
                                    R.layout.item_dropdown_item,
                                    walletNames
                                )

                                binding.autoCompleteWallet.setAdapter(adapter)

                                // Set up the item selected listener
                                binding.autoCompleteWallet.setOnItemClickListener { _, _, position, _ ->
                                    val selectedWallet = if (position == 0) {
                                        null // "All Wallets" selected
                                    } else {
                                        wallets[position - 1] // Adjust index since we added "All Wallets" at position 0
                                    }
                                    viewModel.selectWallet(selectedWallet)
                                }

                                // Set initial selection based on persisted wallet
                                viewModel.selectedWallet.value?.let { selectedWallet ->
                                    val position = wallets.indexOf(selectedWallet) + 1 // +1 because of "All Wallets"
                                    if (position > 0) {
                                        binding.autoCompleteWallet.setText(walletNames[position], false)
                                    } else {
                                        binding.autoCompleteWallet.setText("All Wallets", false)
                                    }
                                } ?: run {
                                    binding.autoCompleteWallet.setText("All Wallets", false)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupViewSwitcher() {
        binding.apply {
            // Set initial state for reports view
            btnReportsView.isSelected = true
            btnTransactionsView.isSelected = false

            btnReportsView.setOnClickListener {
                // Already in reports view, do nothing
            }

            btnTransactionsView.setOnClickListener {
                try {
                    findNavController().navigate(R.id.action_generalReportsFragment_to_generalTransactionsFragment)
                } catch (e: Exception) {
                    Log.e("GeneralReportsFragment", "Navigation error: ${e.message}")
                }
            }
        }
    }

    //================================================================================
    // State Observation
    //================================================================================
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Load data when the fragment starts
                viewModel.loadData()

                // Show expense toggle by default when fragment first loads
                showCategoryExpenseToggle()

                launch {
                    // Observe wallet selection changes
                    viewModel.selectedWallet.collectLatest { selectedWallet ->
                        Log.d("Filtering", "Wallet changed: ${selectedWallet?.name}")
                        updateUI()
                    }
                }

                launch {
                    // Observe date range changes
                    viewModel.dateRange.collectLatest { dateRange ->
                        Log.d("Filtering", "Date range changed: $dateRange")
                        currentDateRange = dateRange?.let { it.start to it.endInclusive }
                        updateDateRangeText()
                        updateUI()
                    }
                }

                launch {
                    // Observe transactions
                    viewModel.transactionsState.collectLatest { state ->
                        when (state) {
                            is GeneralViewModel.TransactionState.Success -> {
                                Log.d("Filtering", "Transactions updated: ${state.transactions.size} items")
                                updateUI()
                            }
                            is GeneralViewModel.TransactionState.Loading -> {
                                // Show loading state if needed
                                Log.d("Filtering", "Loading transactions...")
                            }
                            is GeneralViewModel.TransactionState.Error -> {
                                Log.e("Filtering", "Error loading transactions: ${state.message}")
                                // Show error state if needed
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    // Observe categories
                    viewModel.categoriesState.collectLatest { state ->
                        when (state) {
                            is GeneralViewModel.CategoryState.Success -> {
                                Log.d("Filtering", "Categories updated: ${state.categories.size} items")
                                updateCategoryLists(state.categories)
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    // Observe labels
                    viewModel.labelsState.collectLatest { state ->
                        when (state) {
                            is GeneralViewModel.LabelState.Success -> {
                                Log.d("Filtering", "Labels updated: ${state.labels.size} items")
                                updateLabelLists()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.expenseGoal.collectLatest { goals ->
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

    //================================================================================
    // UI Update Methods
    //================================================================================
    private fun updateUI() {
        when (viewModel.transactionsState.value) {
            is GeneralViewModel.TransactionState.Success -> {
                // Update charts with filtered transactions
                val expenseTransactions = viewModel.getTransactionsByType("expense")
                val incomeTransactions = viewModel.getTransactionsByType("income")

                // Update both charts
                setupLineChart(lineChartExpense, expenseTransactions, "expense")
                setupLineChart(lineChartIncome, incomeTransactions, "income")

                // Update pie chart based on current toggle state
                val isExpense = when {
                    binding.recyclerViewExpenseCategory.visibility == View.VISIBLE -> true
                    binding.recyclerViewIncomeCategory.visibility == View.VISIBLE -> false
                    binding.recyclerViewLabelExpense.visibility == View.VISIBLE -> true
                    binding.recyclerViewLabelIncome.visibility == View.VISIBLE -> false
                    else -> true // Default to expense if no view is visible
                }
                setupPieChart(isExpense)

                // Update category lists if we have categories loaded
                (viewModel.categoriesState.value as? GeneralViewModel.CategoryState.Success)?.let { categoryState ->
                    updateCategoryLists(categoryState.categories)
                }

                // Update label lists if we have labels loaded
                (viewModel.labelsState.value as? GeneralViewModel.LabelState.Success)?.let { labelState ->
                    updateLabelLists()
                }
            }
            is GeneralViewModel.TransactionState.Loading -> {
                // Show loading state
            }
            is GeneralViewModel.TransactionState.Error -> {
                // Show error state
            }
            is GeneralViewModel.TransactionState.Empty -> {
                // Show empty state
            }
        }
    }

    private fun updateCategoryLists(categories: List<Category>) {
        val filteredTransactions = when (val state = viewModel.transactionsState.value) {
            is GeneralViewModel.TransactionState.Success -> state.transactions
            else -> emptyList()
        }

        val expenseTransactions = viewModel.getTransactionsByType("expense")
        val incomeTransactions = viewModel.getTransactionsByType("income")

        val totalExpense = expenseTransactions.sumOf { it.amount.toDouble() }
        val totalIncome = incomeTransactions.sumOf { it.amount.toDouble() }

        binding.apply {
            btnCatExpenseToggle.findViewById<TextView>(R.id.txtExpenseTotal).text =
                CurrencyUtil.formatWithSymbol(-totalExpense)
            btnCatIncomeToggle.findViewById<TextView>(R.id.txtIncomeTotal).text =
                CurrencyUtil.formatWithSymbol(totalIncome)

            // Update chart toggle amounts
            txtChartExpenseTotal.text = CurrencyUtil.formatWithSymbol(-totalExpense)
            txtChartIncomeTotal.text = CurrencyUtil.formatWithSymbol(totalIncome)
        }

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Get expense categories with transactions
        val expenseItems = categories
            .filter { it.type.equals("expense", ignoreCase = true) }
            .map { category ->
                val categoryTransactions = filteredTransactions.filter { it.category.id == category.id }
                Triple(
                    category,
                    categoryTransactions,
                    categoryTransactions.sumOf { it.amount.toDouble() }
                )
            }
            .filter { it.second.isNotEmpty() } // Only keep categories with transactions
            .sortedByDescending { it.third } // Sort by amount in descending order
            .map { (category, transactions, _) ->
                ReportListItems.ReportCategoryItem(
                    category = category,
                    transactionCount = transactions.size,
                    amount = CurrencyUtil.formatWithSymbol(transactions.sumOf { it.amount.toDouble() }),
                    relativeDate = transactions.maxByOrNull { it.date }?.let {
                        dateFormat.format(Date(it.date))
                    } ?: "No transactions"
                )
            }

        // Get income categories with transactions
        val incomeItems = categories
            .filter { it.type.equals("income", ignoreCase = true) }
            .map { category ->
                val categoryTransactions = filteredTransactions.filter { it.category.id == category.id }
                Triple(
                    category,
                    categoryTransactions,
                    categoryTransactions.sumOf { it.amount.toDouble() }
                )
            }
            .filter { it.second.isNotEmpty() } // Only keep categories with transactions
            .sortedByDescending { it.third } // Sort by amount in descending order
            .map { (category, transactions, _) ->
                ReportListItems.ReportCategoryItem(
                    category = category,
                    transactionCount = transactions.size,
                    amount = CurrencyUtil.formatWithSymbol(transactions.sumOf { it.amount.toDouble() }),
                    relativeDate = transactions.maxByOrNull { it.date }?.let {
                        dateFormat.format(Date(it.date))
                    } ?: "No transactions"
                )
            }

        // Calculate new total balance by adding incomes and subtracting expenses
        val newTotalBalance = filteredTransactions.fold(0.0) { total, transaction ->
            if (transaction.category.type.equals("income", ignoreCase = true)) {
                total + transaction.amount
            } else {
                total - transaction.amount
            }
        }
        updateTotalBalance(newTotalBalance)

        // Update both adapters regardless of current view
        categoryExpenseAdapter.submitList(expenseItems)
        categoryIncomeAdapter.submitList(incomeItems)
    }

    private fun updateLabelLists() {
        val filteredTransactions = when (val state = viewModel.transactionsState.value) {
            is GeneralViewModel.TransactionState.Success -> state.transactions
            else -> emptyList()
        }

        // Get all labels
        val labels = when (val state = viewModel.labelsState.value) {
            is GeneralViewModel.LabelState.Success -> state.labels
            else -> emptyList()
        }

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Calculate label totals
        val expenseLabelTotal = filteredTransactions
            .filter { transaction ->
                transaction.labels.isNotEmpty() &&
                transaction.category.type.equals("expense", ignoreCase = true)
            }
            .sumOf { it.amount.toDouble() }

        val incomeLabelTotal = filteredTransactions
            .filter { transaction ->
                transaction.labels.isNotEmpty() &&
                transaction.category.type.equals("income", ignoreCase = true)
            }
            .sumOf { it.amount.toDouble() }

        // Update label toggle totals
        binding.apply {
            btnLabelExpenseToggle.findViewById<TextView>(R.id.txtLabelExpenseTotal).text =
                CurrencyUtil.formatWithSymbol(-expenseLabelTotal)
            btnLabelIncomeToggle.findViewById<TextView>(R.id.txtLabelIncomeTotal).text =
                CurrencyUtil.formatWithSymbol(incomeLabelTotal)
        }

        // Create expense label items
        val expenseLabelItems = labels
            .map { label ->
                val labelTransactions = filteredTransactions.filter { transaction ->
                    transaction.labels.any { it.id == label.id } &&
                    transaction.category.type.equals("expense", ignoreCase = true)
                }
                Triple(
                    label,
                    labelTransactions,
                    labelTransactions.sumOf { it.amount.toDouble() }
                )
            }
            .filter { it.second.isNotEmpty() } // Only keep labels with transactions
            .sortedByDescending { it.third } // Sort by amount in descending order
            .map { (label, transactions, _) ->
                ReportListItems.ReportLabelItem(
                    label = label,
                    transactionCount = transactions.size,
                    amount = CurrencyUtil.formatWithSymbol(transactions.sumOf { it.amount.toDouble() }),
                    relativeDate = transactions.maxByOrNull { it.date }?.let {
                        dateFormat.format(Date(it.date))
                    } ?: "No transactions",
                    type = "expense"
                )
            }

        // Create income label items
        val incomeLabelItems = labels
            .map { label ->
                val labelTransactions = filteredTransactions.filter { transaction ->
                    transaction.labels.any { it.id == label.id } &&
                    transaction.category.type.equals("income", ignoreCase = true)
                }
                Triple(
                    label,
                    labelTransactions,
                    labelTransactions.sumOf { it.amount.toDouble() }
                )
            }
            .filter { it.second.isNotEmpty() } // Only keep labels with transactions
            .sortedByDescending { it.third } // Sort by amount in descending order
            .map { (label, transactions, _) ->
                ReportListItems.ReportLabelItem(
                    label = label,
                    transactionCount = transactions.size,
                    amount = CurrencyUtil.formatWithSymbol(transactions.sumOf { it.amount.toDouble() }),
                    relativeDate = transactions.maxByOrNull { it.date }?.let {
                        dateFormat.format(Date(it.date))
                    } ?: "No transactions",
                    type = "income"
                )
            }

        // Update both adapters regardless of current view
        labelExpenseAdapter.submitList(expenseLabelItems)
        labelIncomeAdapter.submitList(incomeLabelItems)
    }

    private fun updateTotalBalance(balance: Double) {
        viewModel.setTotalBalance(balance)
        binding.apply {
            // Update balance text with current visibility state
            textViewCurrencyTotal.text = if (isBalanceVisible) {
                CurrencyUtil.formatWithoutSymbol(balance)
            } else {
                "••••••"
            }

            // Set text color based on balance
            val colorRes = if (balance >= 0) {
                R.attr.bb_profit
            } else {
                R.attr.bb_expense
            }
            textViewCurrencyTotal.setTextColor(context?.getThemeColor(colorRes) ?: R.color.profit_green)
        }
    }

    private fun updateDateRangeText() {
        binding.tvDateRange.text = if (currentDateRange != null) {
            "${DateUtil.formatDateToDMY(currentDateRange!!.first, true)} -\n${DateUtil.formatDateToDMY(currentDateRange!!.second, true)}"
        } else {
            "Select Date"
        }
    }

    //================================================================================
    // Chart Methods
    //================================================================================
    private fun setupLineChart(chart: LineChart, transactions: List<Transaction>, chartType: String) {
        val dateRange = when (val range = viewModel.dateRange.value) {
            null -> {
                // Default to current month if no date range is set
                val (startDate, endDate) = DateUtil.getCurrentMonthRange()
                startDate..endDate
            }
            else -> range
        }

        GraphUtil.setupLineChart(chart, transactions, chartType, dateRange)

        // Handle expense goal lines if this is the expense chart
        if (chartType == "expense") {
            // Cancel any existing job to prevent leaks
            expenseGoalJob?.cancel()

            expenseGoalJob = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.expenseGoal.collectLatest { goals ->
                    goals?.let { (minGoal, maxGoal) ->
                        Log.d("ExpenseGoal", "Received goals - Min: $minGoal, Max: $maxGoal")
                        updateExpenseGoalLines(minGoal, maxGoal)
                    } ?: run {
                        // Clear the lines if goals are null
                        lineChartExpense.axisLeft.removeAllLimitLines()
                        lineChartExpense.invalidate()
                    }
                }
            }
        }
    }

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

        GraphUtil.setupPieChart(pieChart, allCategories, transactions, isExpense)
    }

    private fun updateExpenseGoalLines(minGoal: Double, maxGoal: Double) {
        Log.d("ExpenseGoalLines", "Updating goal lines - Min: $minGoal, Max: $maxGoal")

        // Only update if we're showing the expense chart
        if (lineChartExpense.visibility != View.VISIBLE) {
            Log.d("ExpenseGoalLines", "Expense chart not visible, skipping update")
            return
        }

        // Get the current chart data
        val entries = (lineChartExpense.data?.dataSets?.firstOrNull() as? LineDataSet)?.values ?: run {
            Log.d("ExpenseGoalLines", "No chart data available")
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

        // Remove existing limit lines
        lineChartExpense.axisLeft.removeAllLimitLines()

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
        lineChartExpense.setExtraOffsets(16f, 40f, 32f, 30f)

        Log.d("ExpenseGoalLines", "Setting axis range - Min: $minAxis, Max: $maxAxis")

        // Apply the new axis range
        lineChartExpense.axisLeft.apply {
            axisMinimum = minAxis
            axisMaximum = maxAxis
            setDrawLimitLinesBehindData(false)  // Changed to false to ensure lines are visible
            setDrawLabels(true)
            setDrawAxisLine(true)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            xOffset = 12f
            setLabelCount(5, true)
            setStartAtZero(false)
        }

        // Force a complete redraw with the new settings
        lineChartExpense.setVisibleXRangeMaximum(entries.size.toFloat())
        lineChartExpense.moveViewToX(0f)
        lineChartExpense.extraTopOffset = 40f
        lineChartExpense.extraBottomOffset = 20f
        lineChartExpense.extraLeftOffset = 16f
        lineChartExpense.extraRightOffset = 32f
        lineChartExpense.invalidate()
        lineChartExpense.requestLayout()

        Log.d("ExpenseGoalLines", "Chart updated with goal lines")
    }

    //================================================================================
    // View Toggle Methods
    //================================================================================
    private fun showCategoryExpenseToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.VISIBLE
            recyclerViewIncomeCategory.visibility = View.GONE
            setupPieChart(isExpense = true)
            highlightToggle(btnCatExpenseToggle, btnCatIncomeToggle)
        }
    }

    private fun showCategoryIncomeToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.GONE
            recyclerViewIncomeCategory.visibility = View.VISIBLE
            setupPieChart(isExpense = false)
            highlightToggle(btnCatIncomeToggle, btnCatExpenseToggle)
        }
    }

    private fun showLabelExpenseToggle() {
        binding.apply {
            recyclerViewLabelExpense.visibility = View.VISIBLE
            recyclerViewLabelIncome.visibility = View.GONE
            highlightToggle(btnLabelExpenseToggle, btnLabelIncomeToggle)
            updateLabelLists()
        }
    }

    private fun showLabelIncomeToggle() {
        binding.apply {
            recyclerViewLabelExpense.visibility = View.GONE
            recyclerViewLabelIncome.visibility = View.VISIBLE
            highlightToggle(btnLabelIncomeToggle, btnLabelExpenseToggle)
            updateLabelLists()
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
                if (totalExpense > 0) CurrencyUtil.formatWithSymbol(-totalExpense)
                else CurrencyUtil.formatWithSymbol(0.0)
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
            val totalIncome = transactions.sumOf { it.amount }
            txtChartIncomeTotal.text =
                if (totalIncome > 0) CurrencyUtil.formatWithSymbol(totalIncome)
                else CurrencyUtil.formatWithSymbol(0.0)
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

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        
        // Use persisted date range if available, otherwise default to current month
        val (defaultStartDate, defaultEndDate) = viewModel.dateRange.value?.let { range ->
            range.start to range.endInclusive
        } ?: DateUtil.getCurrentMonthRange()

        // Create and show the date range picker dialog
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setSelection(
                androidx.core.util.Pair(
                    defaultStartDate,
                    defaultEndDate
                )
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first ?: return@addOnPositiveButtonClickListener
            val endDate = selection.second ?: return@addOnPositiveButtonClickListener
            currentDateRange = startDate to endDate
            updateDateRangeText()
            viewModel.setDateRange(startDate, endDate)
        }

        dateRangePicker.addOnNegativeButtonClickListener {
            currentDateRange = null
            updateDateRangeText()
            viewModel.clearDateRange()
        }

        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    //================================================================================
    // Navigation Methods
    //================================================================================
    private fun navigateToLabelDetails(label: Label) {
        // TODO: Implement navigation to label details
    }

    private fun navigateToTransactionDetails(transaction: Transaction) {
        // TODO: Implement navigation to transaction details
    }

    private fun navigateToCategoryDetails(category: Category) {
        // TODO: Implement navigation to category details
    }
}
