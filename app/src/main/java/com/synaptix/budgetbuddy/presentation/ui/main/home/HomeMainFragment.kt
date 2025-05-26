package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.HomeListItems
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeMainFragment : Fragment() {
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
        setupPieChart()
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
        val barChart: BarChart = binding.barChart

        // Sample data: one month's income and expense
        val income = BarEntry(0f, 5000f) // X = 0
        val expense = BarEntry(1f, 3200f) // X = 1

        val incomeSet = BarDataSet(listOf(income), "Income").apply {
            color = resources.getColor(R.color.expense_red, null)
        }

        val expenseSet = BarDataSet(listOf(expense), "Expense").apply {
            color = resources.getColor(R.color.profit_green, null)
        }

        val data = BarData(incomeSet, expenseSet)

        // Adjust bar width and spacing
        val groupSpace = 0.4f
        val barSpace = 0.05f
        val barWidth = 0.25f

        data.barWidth = barWidth
        barChart.data = data

        // Set X-axis range so both bars fit
        barChart.xAxis.axisMinimum = -0.5f
        barChart.xAxis.axisMaximum = 2f

        // Group the bars
        barChart.groupBars(0f, groupSpace, barSpace)

        // Setup labels
        barChart.xAxis.apply {
            granularity = 1f
            isGranularityEnabled = true
            setDrawGridLines(false)
            setCenterAxisLabels(true)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(listOf("March")) // <-- or dynamically set month
        }

        barChart.description = Description().apply { text = "Monthly Budget" }
        barChart.axisRight.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun setupPieChart() {
        val pieChart: PieChart = binding.pieChart

        // Sample expense categories and values
        val expenseCategories = listOf("Food", "Transport", "Entertainment", "Bills", "Shopping")
        val expenseAmounts = listOf(800f, 400f, 300f, 500f, 600f)

        val pieEntries = expenseCategories.mapIndexed { index, category ->
            PieEntry(expenseAmounts[index], category)
        }

        val pieDataSet = PieDataSet(pieEntries, "Expense Categories").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.cat_dark_green),
                ContextCompat.getColor(requireContext(), R.color.cat_light_pink),
                ContextCompat.getColor(requireContext(), R.color.cat_dark_blue),
                ContextCompat.getColor(requireContext(), R.color.cat_yellow),
                ContextCompat.getColor(requireContext(), R.color.cat_orange)
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 2f
        }

        val pieData = PieData(pieDataSet)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Expenses"
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)

        // ✅ Correct easing import from MPAndroidChart
        pieChart.animateY(1000, Easing.EaseInOutQuad)
        pieChart.invalidate()
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
