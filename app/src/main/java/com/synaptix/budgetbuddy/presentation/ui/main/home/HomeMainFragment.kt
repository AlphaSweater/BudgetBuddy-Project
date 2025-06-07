package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.HomeListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalIndividualTransaction.GeneralIndividualTransactionViewModel
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment for the main home screen.
 * 
 * This fragment is responsible for:
 * 1. Displaying wallet overviews
 * 2. Showing recent transactions
 * 3. Displaying category summaries
 * 4. Visualizing data through charts
 * 5. Handling user navigation
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
 *    - Each data type (wallets, transactions, categories) has its own observer
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
class HomeMainFragment : Fragment() {

    //================================================================================
    // Properties
    //================================================================================
    /**
     * Running totals for income and expenses.
     * Used for calculating and displaying financial summaries.
     */
    private var totalIncome: Double = 0.0
    private var totalExpense: Double = 0.0

    companion object {
        private const val MAX_ITEMS = 3
    }

    /**
     * ViewBinding for safe view access.
     * Nulled in onDestroyView to prevent memory leaks.
     */
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /**
     * ViewModel for data management.
     * Uses activityViewModels to share state across fragments.
     */
    private val homeViewModel: HomeMainViewModel by activityViewModels()

    //================================================================================
    // Adapters
    //================================================================================
    /**
     * Adapter for displaying wallet items.
     * Handles wallet overview cards with balance and recent activity.
     */
    private val walletAdapter by lazy {
        HomeAdapter(
            onWalletClick = { wallet -> navigateToWalletDetails(wallet) }
        )
    }

    /**
     * Adapter for displaying transaction items.
     * Shows transaction details with category and wallet info.
     */
    private val transactionAdapter by lazy {
        HomeAdapter(
            onTransactionClick = { transaction -> navigateToTransactionDetails(transaction) }
        )
    }

    /**
     * Adapter for displaying category items.
     * Shows category summaries with transaction counts and amounts.
     */
    private val categoryAdapter by lazy {
        HomeAdapter(
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeStates()
        setupBarChart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //================================================================================
    // Setup Methods
    //================================================================================
    /**
     * Sets up all views and click listeners.
     * Called during fragment initialization.
     */
    private fun setupViews() {
        setupRecyclerViews()
        setupClickListeners()
    }

    /**
     * Sets up RecyclerViews with their adapters and layout managers.
     * Configures the display of wallets, transactions, and categories.
     */
    private fun setupRecyclerViews() {
        binding.apply {
            recyclerViewHomeWalletOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = walletAdapter
            }

            recyclerViewHomeTransactionOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = transactionAdapter
            }

            recyclerViewHomeCategoryOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryAdapter
            }
        }
    }

    /**
     * Sets up click listeners for navigation and actions.
     * Handles "View All" buttons and other UI interactions.
     */
    private fun setupClickListeners() {
        binding.apply {
            txtViewAllWallets.setOnClickListener { navigateToAllWallets() }
            txtViewAllCategories.setOnClickListener { navigateToAllCategories() }
            txtViewAllTransactions.setOnClickListener { navigateToAllTransactions() }
        }
    }

    //================================================================================
    // Chart Setup Methods
    //================================================================================
    /**
     * Sets up the bar chart showing income vs expense comparison.
     * Configures appearance, data, and interaction settings.
     */
    private fun setupBarChart() {
        val context = binding.root.context
        val barChart: BarChart = binding.barChart

        val primaryTextColor = context.getThemeColor(R.attr.bb_primaryText)
        val expenseColor = context.getThemeColor(R.attr.bb_expense)
        val profitColor = context.getThemeColor(R.attr.bb_profit)

        val incomeEntry = BarEntry(0f, totalIncome.toFloat())
        val expenseEntry = BarEntry(1f, totalExpense.toFloat())

        val incomeSet = BarDataSet(listOf(incomeEntry), "Income").apply {
            color = profitColor
            valueTextColor = primaryTextColor
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R ${String.format("%.2f", value)}"
                }
            }
        }

        val expenseSet = BarDataSet(listOf(expenseEntry), "Expense").apply {
            color = expenseColor
            valueTextColor = primaryTextColor
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R ${String.format("%.2f", value)}"
                }
            }
        }

        val data = BarData(incomeSet, expenseSet).apply {
            barWidth = 0.3f
            setDrawValues(true)
        }

        configureBarChart(barChart, data, primaryTextColor)
    }

    /**
     * Configures the bar chart's appearance and behavior.
     * Sets up axes, legend, and interaction settings.
     */
    private fun configureBarChart(barChart: BarChart, data: BarData, primaryTextColor: Int) {
        barChart.apply {
            this.data = data
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = 1.5f
            groupBars(0f, 0.4f, 0.05f)

            configureXAxis(primaryTextColor)
            configureYAxis(primaryTextColor)
            configureLegend(primaryTextColor)
            configureChartAppearance()
        }
    }

    /**
     * Configures the X-axis of the bar chart.
     * Sets up labels, grid lines, and formatting.
     */
    private fun BarChart.configureXAxis(primaryTextColor: Int) {
        xAxis.apply {
            granularity = 1f
            isGranularityEnabled = true
            setDrawGridLines(false)
            setCenterAxisLabels(true)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(listOf("Income", "Expense"))
            textColor = primaryTextColor
            textSize = 12f
        }
    }

    /**
     * Configures the Y-axis of the bar chart.
     * Sets up grid lines, formatting, and appearance.
     */
    private fun BarChart.configureYAxis(primaryTextColor: Int) {
        axisLeft.apply {
            textColor = primaryTextColor
            textSize = 12f
            setDrawGridLines(true)
            gridColor = context.getThemeColor(R.attr.bb_secondaryText)
            gridLineWidth = 0.5f
            axisLineColor = primaryTextColor
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R ${String.format("%.0f", value)}"
                }
            }
        }
        axisRight.isEnabled = false
    }

    /**
     * Configures the legend of the bar chart.
     * Sets up position, appearance, and formatting.
     */
    private fun BarChart.configureLegend(primaryTextColor: Int) {
        legend.apply {
            textColor = primaryTextColor
            textSize = 12f
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }
    }

    /**
     * Configures the general appearance of the bar chart.
     * Sets up interaction, animation, and visual settings.
     */
    private fun BarChart.configureChartAppearance() {
        description.isEnabled = false
        setDrawGridBackground(false)
        setDrawBorders(false)
        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(true)
        setPinchZoom(false)
        setDrawValueAboveBar(true)
        animateY(1000, Easing.EaseInOutQuad)
        invalidate()
    }

    /**
     * Sets up the pie chart showing category breakdown.
     * Configures appearance, data, and interaction settings.
     */
    private fun setupPieChart() {
        val context = binding.root.context
        val pieChart: PieChart = binding.pieChart

        val (currentCategories, currentTransactions) = getCurrentData()
        val entries = createPieEntries(currentCategories, currentTransactions)

        if (entries.isEmpty()) {
            showEmptyPieChart(pieChart, context)
            return
        } else
            pieChart.visibility = View.VISIBLE

        val pieDataSet = createPieDataSet(entries, currentCategories, context)
        val pieData = createPieData(pieDataSet, context)
        configurePieChart(pieChart, pieData, context)
    }

    /**
     * Gets the current categories and transactions from the ViewModel.
     * Used for creating pie chart entries.
     */
    private fun getCurrentData(): Pair<List<Category>, List<Transaction>> {
        val currentCategories = when (val categoryState = homeViewModel.categoriesState.value) {
            is HomeMainViewModel.CategoryState.Success -> categoryState.categories
            else -> emptyList()
        }

        val currentTransactions = when (val transactionState = homeViewModel.transactionsState.value) {
            is HomeMainViewModel.TransactionState.Success -> transactionState.transactions
            else -> emptyList()
        }

        return Pair(currentCategories, currentTransactions)
    }

    /**
     * Creates pie chart entries from categories and transactions.
     * Groups transactions by category and calculates amounts.
     */
    private fun createPieEntries(
        categories: List<Category>,
        transactions: List<Transaction>
    ): List<PieEntry> {
        val categoryAmounts = transactions
            .filter { it.category.type == "expense" }
            .groupBy { it.category.id }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount.toDouble() } }

        val sortedCategories = categories
            .filter { it.type == "expense" }
            .sortedByDescending { categoryAmounts[it.id] ?: 0.0 }

        return sortedCategories.mapNotNull { category ->
            val amount = categoryAmounts[category.id] ?: 0.0
            if (amount > 0) {
                PieEntry(amount.toFloat(), category.name)
            } else {
                null
            }
        }
    }

    /**
     * Shows an empty state for the pie chart.
     * Displays a message when no data is available.
     */
    private fun showEmptyPieChart(pieChart: PieChart, context: Context) {
        pieChart.clear()
        pieChart.visibility = View.GONE
        pieChart.setNoDataText("No expense data available")
        pieChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
        pieChart.invalidate()
    }

    /**
     * Creates a PieDataSet for the pie chart.
     * Configures colors, values, and formatting.
     */
    private fun createPieDataSet(
        entries: List<PieEntry>,
        categories: List<Category>,
        context: Context
    ): PieDataSet {
        return PieDataSet(entries, "Expense Categories").apply {
            colors = entries.map { entry ->
                val category = categories.find { it.name == entry.label }
                category?.color ?: ContextCompat.getColor(context, R.color.cat_dark_green)
            }

            valueTextColor = context.getThemeColor(R.attr.bb_primaryText)
            valueTextSize = 16f
            valueLineColor = context.getThemeColor(R.attr.bb_secondaryText)
            valueLinePart1Length = 0.6f
            valueLinePart2Length = 0.6f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val total = entries.sumOf { it.value.toDouble() }
                    val percentage = (value / total * 100)
                    return "${String.format("%.1f", percentage)}%"
                }
            }
        }
    }

    /**
     * Creates PieData for the pie chart.
     * Sets up text size and color.
     */
    private fun createPieData(pieDataSet: PieDataSet, context: Context): PieData {
        return PieData(pieDataSet).apply {
            setValueTextSize(16f)
            setValueTextColor(context.getThemeColor(R.attr.bb_primaryText))
        }
    }

    /**
     * Configures the pie chart's appearance and behavior.
     * Sets up hole, center text, and interaction settings.
     */
    private fun configurePieChart(pieChart: PieChart, pieData: PieData, context: Context) {
        pieChart.apply {
            data = pieData
            configurePieChartAppearance(context)
            configurePieChartLegend(context)
            configurePieChartInteraction()
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    /**
     * Configures the general appearance of the pie chart.
     * Sets up hole, center text, and visual settings.
     */
    private fun PieChart.configurePieChartAppearance(context: Context) {
        isDrawHoleEnabled = true
        holeRadius = 60f
        transparentCircleRadius = 65f
        setHoleColor(Color.TRANSPARENT)
        setTransparentCircleColor(context.getThemeColor(R.attr.bb_surface))
        setTransparentCircleAlpha(110)

        centerText = "Expenses"
        setCenterTextColor(context.getThemeColor(R.attr.bb_primaryText))
        setCenterTextTypeface(Typeface.DEFAULT_BOLD)
        setCenterTextSize(20f)

        setUsePercentValues(true)
        setDrawEntryLabels(false)
        description.isEnabled = false

        setExtraOffsets(10f, 10f, 10f, 10f)
    }

    /**
     * Configures the legend of the pie chart.
     * Sets up position, appearance, and formatting.
     */
    private fun PieChart.configurePieChartLegend(context: Context) {
        legend.apply {
            isEnabled = true
            textColor = context.getThemeColor(R.attr.bb_primaryText)
            textSize = 14f
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = Legend.LegendForm.CIRCLE
            formSize = 14f
            formToTextSpace = 10f
            xEntrySpace = 15f
            yEntrySpace = 5f
        }
    }

    /**
     * Configures the interaction settings of the pie chart.
     * Sets up touch, rotation, and highlight behavior.
     */
    private fun PieChart.configurePieChartInteraction() {
        setTouchEnabled(false)
        isRotationEnabled = false
        rotationAngle = 0f
        isHighlightPerTapEnabled = false
        setDrawEntryLabels(false)
    }

    //================================================================================
    // State Observation
    //================================================================================
    /**
     * Sets up state observation using coroutines.
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
                // Launch parallel coroutines for each data type
                launch { observeTransactions() }
                launch { observeWallets() }
                launch { observeCategories() }
            }
        }
    }

    /**
     * Observes transaction state changes.
     * 
     * Flow Collection Process:
     * 1. Collects the latest value from transactionsState
     * 2. Updates UI based on the current state:
     *    - Success: Updates transaction list and charts
     *    - Empty: Shows empty state
     *    - Error: Shows error state
     *    - Loading: Shows loading indicator
     * 
     * When transactions are loaded:
     * 1. Updates total income and expense
     * 2. Refreshes the bar chart
     * 3. Updates the transaction list
     * 4. Triggers category state collection for pie chart
     */
    private suspend fun observeTransactions() {
        homeViewModel.transactionsState.collectLatest { state ->
            when (state) {
                is HomeMainViewModel.TransactionState.Success -> {
                    // Update totals and charts
                    updateTransactionTotals(state.transactions)
                    setupBarChart()
                    handleTransactionsState(state)
                    
                    // Collect category state for pie chart
                    homeViewModel.categoriesState.collect { state ->
                        handleCategoriesState(state)
                        setupPieChart()
                    }
                }
                is HomeMainViewModel.TransactionState.Empty -> {
                    handleTransactionsState(state)
                }
                else -> handleTransactionsState(state)
            }
        }
    }

    /**
     * Observes wallet state changes.
     * 
     * Flow Collection Process:
     * 1. Collects the latest value from walletsState
     * 2. Updates UI based on the current state
     * 3. Handles loading, success, error, and empty states
     * 
     * The wallet list is updated with:
     * - Wallet name and balance
     * - Last transaction date
     * - Wallet icon
     */
    private suspend fun observeWallets() {
        homeViewModel.walletsState.collectLatest { state ->
            handleWalletsState(state)
        }
    }

    /**
     * Observes category state changes.
     * 
     * Flow Collection Process:
     * 1. Collects the latest value from categoriesState
     * 2. Updates UI based on the current state
     * 3. Refreshes the pie chart with new data
     * 
     * The category list is updated with:
     * - Category name and icon
     * - Transaction count
     * - Total amount
     * - Most recent transaction date
     */
    private suspend fun observeCategories() {
        homeViewModel.categoriesState.collectLatest { state ->
            handleCategoriesState(state)
            setupPieChart()
        }
    }

    //================================================================================
    // State Handlers
    //================================================================================
    /**
     * Handles wallet state changes.
     * Updates UI based on the current state.
     */
    private fun handleWalletsState(state: HomeMainViewModel.WalletState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.WalletState.Loading -> {
                    showLoadingState(
                        recyclerView = recyclerViewHomeWalletOverview,
                        progressBar = progressBarWallets,
                        emptyText = txtEmptyWallets
                    )
                }
                is HomeMainViewModel.WalletState.Success -> {
                    hideLoadingState(progressBarWallets)
                    showContentState(
                        recyclerView = recyclerViewHomeWalletOverview,
                        emptyText = txtEmptyWallets
                    )

                    val walletItems = state.wallets.take(MAX_ITEMS).map { wallet ->
                        HomeListItems.HomeWalletItem(
                            wallet = wallet,
                            walletIcon = R.drawable.ic_ui_wallet,
                            relativeDate = wallet.formatDate(wallet.lastTransactionAt)
                        )
                    }
                    walletAdapter.submitList(walletItems)
                }
                is HomeMainViewModel.WalletState.Empty -> {
                    hideLoadingState(progressBarWallets)
                    showEmptyState(
                        recyclerView = recyclerViewHomeWalletOverview,
                        emptyText = txtEmptyWallets,
                        message = getString(R.string.no_wallets_found)
                    )
                }
                is HomeMainViewModel.WalletState.Error -> {
                    hideLoadingState(progressBarWallets)
                    showEmptyState(
                        recyclerView = recyclerViewHomeWalletOverview,
                        emptyText = txtEmptyWallets,
                        message = getString(R.string.no_wallets_found)
                    )
                }
            }
        }
    }

    /**
     * Handles transaction state changes.
     * Updates UI based on the current state.
     */
    private fun handleTransactionsState(state: HomeMainViewModel.TransactionState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.TransactionState.Loading -> {
                    showLoadingState(
                        recyclerView = recyclerViewHomeTransactionOverview,
                        progressBar = progressBarTransactions,
                        emptyText = txtEmptyTransactions
                    )
                }
                is HomeMainViewModel.TransactionState.Success -> {
                    hideLoadingState(progressBarTransactions)
                    showContentState(
                        recyclerView = recyclerViewHomeTransactionOverview,
                        emptyText = txtEmptyTransactions
                    )

                    val transactionItems = state.transactions.take(MAX_ITEMS).map { transaction ->
                        HomeListItems.HomeTransactionItem(
                            transaction = transaction,
                            relativeDate = transaction.formatDate(transaction.date)
                        )
                    }
                    transactionAdapter.submitList(transactionItems)
                }
                is HomeMainViewModel.TransactionState.Empty -> {
                    hideLoadingState(progressBarTransactions)
                    showEmptyState(
                        recyclerView = recyclerViewHomeTransactionOverview,
                        emptyText = txtEmptyTransactions,
                        message = getString(R.string.no_transactions_found)
                    )
                }
                is HomeMainViewModel.TransactionState.Error -> {
                    hideLoadingState(progressBarTransactions)
                    showEmptyState(
                        recyclerView = recyclerViewHomeTransactionOverview,
                        emptyText = txtEmptyTransactions,
                        message = getString(R.string.no_transactions_found)
                    )
                }
            }
        }
    }

    /**
     * Handles category state changes.
     * Updates UI based on the current state.
     */
    private fun handleCategoriesState(state: HomeMainViewModel.CategoryState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.CategoryState.Loading -> {
                    showLoadingState(
                        recyclerView = recyclerViewHomeCategoryOverview,
                        progressBar = progressBarCategories,
                        emptyText = txtEmptyCategories
                    )
                }
                is HomeMainViewModel.CategoryState.Success -> {
                    hideLoadingState(progressBarCategories)
                    showContentState(
                        recyclerView = recyclerViewHomeCategoryOverview,
                        emptyText = txtEmptyCategories
                    )

                    val currentTransactions = when (val transactionState = homeViewModel.transactionsState.value) {
                        is HomeMainViewModel.TransactionState.Success -> transactionState.transactions
                        else -> emptyList()
                    }

                    val categoryItems = state.categories.take(MAX_ITEMS).map { category ->
                        val categoryTransactions = currentTransactions.filter { it.category.id == category.id }
                        val transactionCount = categoryTransactions.size
                        val totalAmount = categoryTransactions.sumOf { it.amount }
                        val formattedAmount = String.format("R %.2f", totalAmount)
                        val mostRecentDate = categoryTransactions.maxOfOrNull { it.date } ?: System.currentTimeMillis()

                        HomeListItems.HomeCategoryItem(
                            category = category,
                            transactionCount = transactionCount,
                            amount = formattedAmount,
                            relativeDate = category.formatDate(mostRecentDate)
                        )
                    }
                    categoryAdapter.submitList(categoryItems)
                }
                is HomeMainViewModel.CategoryState.Empty -> {
                    hideLoadingState(progressBarCategories)
                    showEmptyState(
                        recyclerView = recyclerViewHomeCategoryOverview,
                        emptyText = txtEmptyCategories,
                        message = getString(R.string.no_categories_found)
                    )
                }
                is HomeMainViewModel.CategoryState.Error -> {
                    hideLoadingState(progressBarCategories)
                    showEmptyState(
                        recyclerView = recyclerViewHomeCategoryOverview,
                        emptyText = txtEmptyCategories,
                        message = getString(R.string.no_categories_found)
                    )
                }
            }
        }
    }

    //================================================================================
    // UI State Helpers
    //================================================================================
    /**
     * Shows loading state for a section.
     * Hides content and shows progress indicator.
     */
    private fun showLoadingState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        progressBar: View,
        emptyText: TextView
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = false
        progressBar.isVisible = true
    }

    /**
     * Hides loading state.
     * Removes progress indicator.
     */
    private fun hideLoadingState(progressBar: View) {
        progressBar.isVisible = false
    }

    /**
     * Shows empty state for a section.
     * Displays a message when no data is available.
     */
    private fun showEmptyState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        emptyText: TextView,
        message: String
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = true
        emptyText.text = message
    }

    /**
     * Shows content state for a section.
     * Displays the RecyclerView with data.
     */
    private fun showContentState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        emptyText: TextView
    ) {
        recyclerView.isVisible = true
        emptyText.isVisible = false
    }

    //================================================================================
    // Navigation
    //================================================================================
    /**
     * Navigates to wallet details screen.
     * To be implemented when the details screen is ready.
     */
    private fun navigateToWalletDetails(wallet: Wallet) {
        // TODO: Implement navigation to wallet details
    }

    /**
     * Navigates to transaction details screen.
     * To be implemented when the details screen is ready.
     */
    private fun navigateToTransactionDetails(transaction: Transaction) {
        Log.d("HomeFragment", "Navigating to transaction details: ${transaction.id}")
        // Set the transaction in the destination fragment's ViewModel first
        val transactionViewModel: TransactionAddViewModel by activityViewModels()
        transactionViewModel.setTransaction(transaction)

        val bundle = bundleOf("screenMode" to TransactionAddViewModel.ScreenMode.VIEW)
        findNavController().navigate(
            R.id.action_homeFragment_to_transactionAddFragment,
            bundle
        )
    }

    /**
     * Navigates to category details screen.
     * To be implemented when the details screen is ready.
     */
    private fun navigateToCategoryDetails(category: Category) {
        // TODO: Implement navigation to category details
    }

    /**
     * Navigates to all wallets screen.
     * To be implemented when the screen is ready.
     */
    private fun navigateToAllWallets() {
        findNavController().navigate(R.id.action_homeFragment_to_walletMainFragment)
    }

    /**
     * Navigates to all categories screen.
     * To be implemented when the screen is ready.
     */
    private fun navigateToAllCategories() {
        findNavController().navigate(R.id.action_homeFragment_to_generalReportsFragment)
    }

    /**
     * Navigates to all transactions screen.
     * Currently navigates to the reports screen.
     */
    private fun navigateToAllTransactions() {
        findNavController().navigate(R.id.action_homeFragment_to_generalTransactionsFragment)
    }

    //================================================================================
    // Utility Functions
    //================================================================================
    /**
     * Gets a color from the current theme.
     * Used for consistent theming across the app.
     * 
     * @param attrRes The attribute resource ID for the color
     * @return The resolved color value
     */
    fun Context.getThemeColor(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    private fun HomeMainFragment.updateTransactionTotals(transactions: List<Transaction>) {
        totalIncome = transactions.filter { it.category.type == "income" }
            .sumOf { it.amount.toDouble() }
        totalExpense = transactions.filter { it.category.type == "expense" }
            .sumOf { it.amount.toDouble() }
    }
}