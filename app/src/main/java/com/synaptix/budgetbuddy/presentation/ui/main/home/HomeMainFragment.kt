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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.HomeListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.core.util.DateUtil
import com.synaptix.budgetbuddy.core.util.PrivacyUtil
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
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
    private var isBalanceVisible = true

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
        observeViewModel()
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
        setupBarChart()
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
            
            // Add eye icon click listener
            btnViewEye.setOnClickListener {
                isBalanceVisible = PrivacyUtil.toggleBalanceVisibility(
                    isVisible = isBalanceVisible,
                    balanceView = textViewCurrencyTotal,
                    eyeIcon = imageViewEye,
                    balance = homeViewModel.totalWalletBalance.value
                )
            }
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
                    return CurrencyUtil.formatWithSymbol(value.toDouble())
                }
            }
        }

        val expenseSet = BarDataSet(listOf(expenseEntry), "Expense").apply {
            color = expenseColor
            valueTextColor = primaryTextColor
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return CurrencyUtil.formatWithSymbol(value.toDouble())
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
                    return CurrencyUtil.formatWithSymbol(value.toDouble())
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
        visibility = View.VISIBLE
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
                            walletIcon = R.drawable.ic_ui_wallet
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

                    // Update transaction totals from the summary
                    totalIncome = state.summary.totalIncome
                    totalExpense = state.summary.totalExpense
                    setupBarChart()

                    val transactionItems = state.transactions.take(MAX_ITEMS).map { transaction ->
                        HomeListItems.HomeTransactionItem(
                            transaction = transaction,
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
                        message = state.message
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

                    val categoryItems = state.categories.take(MAX_ITEMS).map { category ->
                        val categorySummary = state.categorySummaries[category.id]
                        val formattedAmount = categorySummary?.let {
                            CurrencyUtil.formatWithSymbol(it.totalExpense)
                        } ?: CurrencyUtil.formatWithSymbol(0.0)

                        HomeListItems.HomeCategoryItem(
                            category = category,
                            transactionCount = categorySummary?.transactionCount ?: 0,
                            amount = formattedAmount,
                            lastActivityAt = categorySummary?.lastTransactionAt ?: 0L
                        )
                    }
                    categoryAdapter.submitList(categoryItems)
                }
                is HomeMainViewModel.CategoryState.Empty -> {
                    hideLoadingState(progressBarCategories)
                    showEmptyState(
                        recyclerView = recyclerViewHomeCategoryOverview,
                        emptyText = txtEmptyCategories,
                        message = getString(R.string.no_expense_categories_found)
                    )
                }
                is HomeMainViewModel.CategoryState.Error -> {
                    hideLoadingState(progressBarCategories)
                    showEmptyState(
                        recyclerView = recyclerViewHomeCategoryOverview,
                        emptyText = txtEmptyCategories,
                        message = state.message
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
     */
    private fun navigateToTransactionDetails(transaction: Transaction) {
        Log.d("HomeFragment", "Navigating to transaction details: ${transaction.id}")

        val bundle = bundleOf(
            "screenMode" to TransactionAddViewModel.ScreenMode.VIEW,
            "transactionId" to transaction.id
        )

        findNavController().navigate(
            R.id.ind_transaction_navigation_graph,
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
        val bundle = bundleOf("startDestination" to "generalReportsFragment")
        findNavController().navigate(R.id.action_homeFragment_to_reportNavigationGraph, bundle)
    }

    /**
     * Navigates to all transactions screen.
     * Currently navigates to the reports screen.
     */
    private fun navigateToAllTransactions() {
        val bundle = bundleOf("startDestination" to "generalTransactionsFragment")
        findNavController().navigate(R.id.action_homeFragment_to_reportNavigationGraph, bundle)
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

    //================================================================================
    // ViewModel Observers
    //================================================================================
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { homeViewModel.walletsState.collect { state ->
                    handleWalletsState(state)
                } }
                launch { homeViewModel.transactionsState.collect { state ->
                    handleTransactionsState(state)
                } }
                launch { homeViewModel.categoriesState.collect { state ->
                    handleCategoriesState(state)
                } }
                launch { homeViewModel.totalWalletBalance.collect { balance ->
                    updateTotalBalance(balance)
                } }
            }
        }
    }

    private fun updateTotalBalance(balance: Double?) {
        binding.apply {
            // Update balance text with current visibility state
            textViewCurrencyTotal.text = if (isBalanceVisible) {
                CurrencyUtil.formatWithoutSymbol(balance)
            } else {
                "••••••"
            }
            
            // Set text color based on balance
            val colorRes = if (balance != null && balance >= 0) {
                R.attr.bb_profit
            } else {
                R.attr.bb_expense
            }
            textViewCurrencyTotal.setTextColor(context?.getThemeColor(colorRes) ?: R.color.profit_green)
        }
    }
}