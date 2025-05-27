package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
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

    private var totalIncome: Double = 0.0
    private var totalExpense: Double = 0.0

    companion object {
        private const val MAX_ITEMS = 3
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeMainViewModel by activityViewModels()

    // Initialize adapters once and reuse them
    private val walletAdapter by lazy {
        HomeAdapter(
            onWalletClick = { wallet ->
                // TODO: Implement wallet click handling
                navigateToWalletDetails(wallet)
            }
        )
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val transactionAdapter by lazy {
        HomeAdapter(
            onTransactionClick = { transaction ->
                // TODO: Implement transaction click handling
                navigateToTransactionDetails(transaction)
            }
        )
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val categoryAdapter by lazy {
        HomeAdapter(
            onCategoryClick = { category ->
                // TODO: Implement category click handling
                navigateToCategoryDetails(category)
            }
        )
    }

    fun Context.getThemeColor(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }


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
        homeViewModel.pieEntries.observe(viewLifecycleOwner) { pieEntries ->
            setupPieChart(pieEntries)
        }
    }

    private fun setupViews() {
        binding.apply {
            // Setup RecyclerViews with their adapters
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

            // Setup click listeners
            txtViewAllWallets.setOnClickListener {
                navigateToAllWallets()
            }

            txtViewAllCategories.setOnClickListener {
                navigateToAllCategories()
            }

            txtViewAllTransactions.setOnClickListener {
                navigateToAllTransactions()
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect all states in parallel but handle dependencies
                var transactionsLoaded = false
                var walletsLoaded = false
                var categoriesLoaded = false

                // Collect transactions first since categories depend on them
                launch {
                    homeViewModel.transactionsState.collectLatest { state ->
                        when (state) {
                            is HomeMainViewModel.TransactionState.Success -> {
                                transactionsLoaded = true

                                totalIncome = state.transactions
                                    .filter { it.category.type == "income" }
                                    .sumOf { it.amount.toDouble() }

                                totalExpense = state.transactions
                                    .filter { it.category.type == "expense" }
                                    .sumOf { it.amount.toDouble() }

                                setupBarChart()

                                handleTransactionsState(state)
                                // If categories are already loaded, refresh them with new transaction data
                                if (categoriesLoaded) {
                                    homeViewModel.categoriesState.value.let { handleCategoriesState(it) }
                                }
                            }
                            is HomeMainViewModel.TransactionState.Empty -> {
                                transactionsLoaded = true
                                handleTransactionsState(state)
                                // If categories are already loaded, refresh them with empty transaction data
                                if (categoriesLoaded) {
                                    homeViewModel.categoriesState.value.let { handleCategoriesState(it) }
                                }
                            }
                            else -> {
                                transactionsLoaded = false
                                handleTransactionsState(state)
                            }
                        }
                    }
                }

                // Collect wallets
                launch {
                    homeViewModel.walletsState.collectLatest { state ->
                        when (state) {
                            is HomeMainViewModel.WalletState.Success -> {
                                walletsLoaded = true
                                handleWalletsState(state)
                            }
                            is HomeMainViewModel.WalletState.Empty -> {
                                walletsLoaded = true
                                handleWalletsState(state)
                            }
                            else -> {
                                walletsLoaded = false
                                handleWalletsState(state)
                            }
                        }
                    }
                }

                // Collect categories last since they depend on transactions
                launch {
                    homeViewModel.categoriesState.collectLatest { state ->
                        when (state) {
                            is HomeMainViewModel.CategoryState.Success -> {
                                categoriesLoaded = true
                                if (transactionsLoaded) {
                                    handleCategoriesState(state)
                                } else {
                                    // Show loading if we don't have transactions yet
                                    showLoadingState(
                                        recyclerView = binding.recyclerViewHomeCategoryOverview,
                                        progressBar = binding.progressBarCategories,
                                        emptyText = binding.txtEmptyCategories
                                    )
                                }
                            }
                            is HomeMainViewModel.CategoryState.Empty -> {
                                categoriesLoaded = true
                                handleCategoriesState(state)
                            }
                            else -> {
                                categoriesLoaded = false
                                handleCategoriesState(state)
                            }
                        }
                    }
                }
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Handle different states for wallets, transactions, and categories

    private fun setupPieChart() {
        val context = binding.root.context
        val pieChart: PieChart = binding.pieChart

        val expenseCategories = listOf("Food", "Transport", "Entertainment", "Bills", "Shopping")
        val expenseAmounts = listOf(800f, 400f, 300f, 500f, 600f)

        val pieEntries = expenseCategories.mapIndexed { index, category ->
            PieEntry(expenseAmounts[index], category)
        }

        val pieDataSet = PieDataSet(pieEntries, "Expense Categories").apply {
            colors = listOf(
                ContextCompat.getColor(context, R.color.cat_dark_green),
                ContextCompat.getColor(context, R.color.cat_light_pink),
                ContextCompat.getColor(context, R.color.cat_dark_blue),
                ContextCompat.getColor(context, R.color.cat_yellow),
                ContextCompat.getColor(context, R.color.cat_orange)
            )
            valueTextSize = 14f
            valueTextColor = context.getThemeColor(R.attr.bb_primaryText)
        }

        val pieData = PieData(pieDataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.apply {
            data = pieData
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            centerText = "Expenses"
            setCenterTextColor(context.getThemeColor(R.attr.bb_primaryText))
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
        }

        val expenseSet = BarDataSet(listOf(expenseEntry), "Expense").apply {
            color = expenseColor
            valueTextColor = primaryTextColor
        }

        val data = BarData(incomeSet, expenseSet).apply {
            barWidth = 0.25f
        }

        barChart.apply {
            this.data = data
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = 2f
            groupBars(0f, 0.4f, 0.05f)

            xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                setCenterAxisLabels(true)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(listOf("March"))
                textColor = primaryTextColor
            }

            axisLeft.textColor = primaryTextColor
            axisRight.isEnabled = false
            legend.textColor = primaryTextColor

            description = Description().apply {
                text = "Monthly Budget"
                textColor = primaryTextColor
            }

            animateY(1000)
            invalidate()
        }
    }

    private fun setupPieChart(pieEntries: List<PieEntry>) {
        val context = binding.root.context
        val pieChart: PieChart = binding.pieChart

        val pieDataSet = PieDataSet(pieEntries, "Transactions per Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = ContextCompat.getColor(context, R.color.light_text)
            valueTextSize = 14f
        }

        val pieData = PieData(pieDataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.apply {
            data = pieData
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            centerText = "Transactions"
            setCenterTextColor(context.getThemeColor(R.attr.bb_primaryText))
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(ContextCompat.getColor(context, R.color.light_text))
            setEntryLabelTextSize(12f)
            description.isEnabled = false
            legend.isEnabled = false
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Handle different states for transactions
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Handle different states for categories
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

                    // Get current transactions from the state flow
                    val currentTransactions = when (val transactionState = homeViewModel.transactionsState.value) {
                        is HomeMainViewModel.TransactionState.Success -> transactionState.transactions
                        else -> emptyList()
                    }

                    // Create and display category items immediately
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

    // Helper functions for UI state management
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

    private fun showErrorState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        emptyText: TextView,
        message: String
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = true
        emptyText.text = message
        showError(message)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Navigation functions
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\