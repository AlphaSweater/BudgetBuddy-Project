package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.synaptix.budgetbuddy.data.firebase.model.TransactionDTO
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
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

    private val viewModel: HomeMainViewModel by activityViewModels()

    // Initialize adapters once and reuse them
    private val walletAdapter by lazy {
        HomeAdapter(
            onWalletClick = { wallet ->
                // TODO: Implement wallet click handling:
                // 1. Navigate to wallet details screen
                // 2. Pass wallet data using Safe Args:
                //    - wallet name
                //    - wallet balance
                // 3. Show wallet transactions for this specific wallet
            }
        )
    }

    private val transactionAdapter by lazy {
        HomeAdapter(
            onTransactionClick = { transaction ->
                // TODO: Implement transaction click handling:
                // 1. Navigate to transaction details screen
                // 2. Pass transaction data using Safe Args:
                //    - transaction amount
                //    - category details
                //    - wallet details
                //    - date and notes
                // 3. Allow editing and viewing of transaction
            }
        )
    }

    private val categoryAdapter by lazy {
        HomeAdapter(
            onCategoryClick = { category ->
                // TODO: Implement category click handling:
                // 1. Navigate to category details screen
                // 2. Pass category data using Safe Args:
                //    - category name
                //    - category icon
                //    - category color
                // 3. Show transactions for this specific category
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
        viewModel.pieEntries.observe(viewLifecycleOwner) { pieEntries ->
            setupPieChart(pieEntries)
        }
    }

    private fun setupViews() {
        binding.apply {
            //editTextDate2.setOnClickListener { openDateRangePicker() }

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
                // TODO: Navigate to all wallets
            }

            txtViewAllCategories.setOnClickListener {
                // TODO: Navigate to all categories
            }

            txtViewAllTransactions.setOnClickListener {
                // TODO: Navigate to all transactions
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.walletsState.collect { state ->
                        handleWalletsState(state)
                    }
                }
                launch {
                    viewModel.transactionsState.collect { state ->
                        handleTransactionsState(state)

                        if (state is HomeMainViewModel.TransactionState.Success) {
                            totalIncome = state.transactions
                                .filter { it.category.type == "income" }
                                .sumOf { it.amount.toDouble() }

                            totalExpense = state.transactions
                                .filter { it.category.type == "expense" }
                                .sumOf { it.amount.toDouble() }

                            setupBarChart()
                        }

                    }
                }
                launch {
                    viewModel.categoriesState.collect { state ->
                        handleCategoriesState(state)
                    }
                }
            }
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
                    recyclerViewHomeWalletOverview.isVisible = false
                    txtEmptyWallets.isVisible = false
                    // TODO: Show loading indicator
                }

                is HomeMainViewModel.WalletState.Success -> {
                    val wallets = state.wallets
                    if (wallets.isEmpty()) {
                        recyclerViewHomeWalletOverview.isVisible = false
                        txtEmptyWallets.isVisible = true
                        return
                    }

                    recyclerViewHomeWalletOverview.isVisible = true
                    txtEmptyWallets.isVisible = false

                    val walletItems = wallets.take(MAX_ITEMS).map { wallet ->
                        HomeListItems.HomeWalletItem(
                            wallet = wallet,
                            walletIcon = R.drawable.ic_wallet_24,
                            relativeDate = "Recent" // TODO: Calculate actual relative date
                        )
                    }
                    walletAdapter.submitList(walletItems)
                }

                is HomeMainViewModel.WalletState.Error -> {
                    recyclerViewHomeWalletOverview.isVisible = false
                    txtEmptyWallets.isVisible = true
                    txtEmptyWallets.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun handleTransactionsState(state: HomeMainViewModel.TransactionState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.TransactionState.Loading -> {
                    recyclerViewHomeTransactionOverview.isVisible = false
                    txtEmptyTransactions.isVisible = false
                    // TODO: Show loading indicator
                }

                is HomeMainViewModel.TransactionState.Success -> {
                    val transactions = state.transactions
                    if (transactions.isEmpty()) {
                        recyclerViewHomeTransactionOverview.isVisible = false
                        txtEmptyTransactions.isVisible = true
                        return
                    }

                    recyclerViewHomeTransactionOverview.isVisible = true
                    txtEmptyTransactions.isVisible = false

                    val transactionItems = transactions.take(MAX_ITEMS).map { transaction ->
                        HomeListItems.HomeTransactionItem(
                            transaction = transaction,
                            relativeDate = "Recent" // TODO: Calculate actual relative date
                        )
                    }
                    transactionAdapter.submitList(transactionItems)
                }

                is HomeMainViewModel.TransactionState.Error -> {
                    recyclerViewHomeTransactionOverview.isVisible = false
                    txtEmptyTransactions.isVisible = true
                    txtEmptyTransactions.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun handleCategoriesState(state: HomeMainViewModel.CategoryState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.CategoryState.Loading -> {
                    recyclerViewHomeCategoryOverview.isVisible = false
                    txtEmptyCategories.isVisible = false
                    // TODO: Show loading indicator
                }

                is HomeMainViewModel.CategoryState.Success -> {
                    val categories = state.categories
                    if (categories.isEmpty()) {
                        recyclerViewHomeCategoryOverview.isVisible = false
                        txtEmptyCategories.isVisible = true
                        return
                    }

                    recyclerViewHomeCategoryOverview.isVisible = true
                    txtEmptyCategories.isVisible = false

                    val categoryItems = categories.take(MAX_ITEMS).map { category ->
                        HomeListItems.HomeCategoryItem(
                            category = category,
                            transactionCount = 0, // TODO: Calculate actual count
                            amount = "0.00", // TODO: Calculate actual amount
                            relativeDate = "Recent" // TODO: Calculate actual relative date
                        )
                    }
                    categoryAdapter.submitList(categoryItems)
                }

                is HomeMainViewModel.CategoryState.Error -> {
                    recyclerViewHomeCategoryOverview.isVisible = false
                    txtEmptyCategories.isVisible = true
                    txtEmptyCategories.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}
