package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.animation.Easing
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeMainFragment : Fragment() {

    //================================================================================
    // Properties
    //================================================================================
    private var totalIncome: Double = 0.0
    private var totalExpense: Double = 0.0

    companion object {
        private const val MAX_ITEMS = 3
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeMainViewModel by activityViewModels()

    //================================================================================
    // Adapters
    //================================================================================
    private val walletAdapter by lazy {
        HomeAdapter(
            onWalletClick = { wallet -> navigateToWalletDetails(wallet) }
        )
    }

    private val transactionAdapter by lazy {
        HomeAdapter(
            onTransactionClick = { transaction -> navigateToTransactionDetails(transaction) }
        )
    }

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
    private fun setupViews() {
        setupRecyclerViews()
        setupClickListeners()
    }

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

    private fun createPieEntries(
        categories: List<Category>,
        transactions: List<Transaction>
    ): List<PieEntry> {
        val categoryAmounts = transactions
            .filter { it.category.type == "expense" }
            .groupBy { it.category.id }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount.toDouble() } }

        // Sort the entries by amount in descending order
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

    private fun showEmptyPieChart(pieChart: PieChart, context: Context) {
        pieChart.clear()
        pieChart.visibility = View.GONE
        pieChart.setNoDataText("No expense data available")
        pieChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))
        pieChart.invalidate()
    }

    private fun createPieDataSet(
        entries: List<PieEntry>,
        categories: List<Category>,
        context: Context
    ): PieDataSet {
        return PieDataSet(entries, "Expense Categories").apply {
            colors = entries.mapIndexed { index, entry ->
                val category = categories.find { it.name == entry.label }
                category?.color ?: when (index % 6) {
                    0 -> ContextCompat.getColor(context, R.color.cat_dark_green)
                    1 -> ContextCompat.getColor(context, R.color.cat_light_pink)
                    2 -> ContextCompat.getColor(context, R.color.cat_dark_blue)
                    3 -> ContextCompat.getColor(context, R.color.cat_yellow)
                    4 -> ContextCompat.getColor(context, R.color.cat_orange)
                    else -> ContextCompat.getColor(context, R.color.cat_dark_brown)
                }
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

    private fun createPieData(pieDataSet: PieDataSet, context: Context): PieData {
        return PieData(pieDataSet).apply {
            setValueTextSize(16f)
            setValueTextColor(context.getThemeColor(R.attr.bb_primaryText))
        }
    }

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
    }

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
        }
    }

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
    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Launch separate coroutines for each observer
                launch { observeTransactions() }
                launch { observeWallets() }
                launch { observeCategories() }
            }
        }
    }

    private suspend fun observeTransactions() {
        homeViewModel.transactionsState.collectLatest { state ->
            when (state) {
                is HomeMainViewModel.TransactionState.Success -> {
                    updateTransactionTotals(state.transactions)
                    setupBarChart()
                    handleTransactionsState(state)
                }
                is HomeMainViewModel.TransactionState.Empty -> {
                    handleTransactionsState(state)
                }
                else -> handleTransactionsState(state)
            }
        }
    }

    private fun updateTransactionTotals(transactions: List<Transaction>) {
        totalIncome = transactions
            .filter { it.category.type == "income" }
            .sumOf { it.amount.toDouble() }

        totalExpense = transactions
            .filter { it.category.type == "expense" }
            .sumOf { it.amount.toDouble() }
    }

    private suspend fun observeWallets() {
        homeViewModel.walletsState.collectLatest { state ->
            handleWalletsState(state)
        }
    }

    private suspend fun observeCategories() {
        homeViewModel.categoriesState.collectLatest { state ->
            handleCategoriesState(state)
            setupPieChart()
        }
    }

    //================================================================================
    // State Handlers
    //================================================================================
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
    private fun showLoadingState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        progressBar: View,
        emptyText: TextView
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = false
        progressBar.isVisible = true
    }

    private fun hideLoadingState(progressBar: View) {
        progressBar.isVisible = false
    }

    private fun showEmptyState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        emptyText: TextView,
        message: String
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = true
        emptyText.text = message
    }

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
    private fun navigateToWalletDetails(wallet: Wallet) {
        // TODO: Implement navigation to wallet details
    }

    private fun navigateToTransactionDetails(transaction: Transaction) {
        // TODO: Implement navigation to transaction details
    }

    private fun navigateToCategoryDetails(category: Category) {
        // TODO: Implement navigation to category details
    }

    private fun navigateToAllWallets() {
        // TODO: Implement navigation to all wallets
    }

    private fun navigateToAllCategories() {
        // TODO: Implement navigation to all categories
    }

    private fun navigateToAllTransactions() {
        findNavController().navigate(R.id.action_homeFragment_to_generalTransactionsFragment)
    }

    //================================================================================
    // Utility Functions
    //================================================================================
    fun Context.getThemeColor(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }
}