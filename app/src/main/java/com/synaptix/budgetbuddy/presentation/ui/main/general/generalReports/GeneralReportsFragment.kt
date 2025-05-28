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

@AndroidEntryPoint
class GeneralReportsFragment : Fragment() {

    private var _binding: FragmentGeneralReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralReportsViewModel by viewModels()

    private val expenseAdapter by lazy {
        GeneralReportAdapter(
            onTransactionClick = { transaction -> navigateToTransactionDetails(transaction) },
            onCategoryClick = { category -> navigateToCategoryDetails(category) }
        )
    }

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

    private fun updateTransactionLists(transactions: List<Transaction>) {
        val expenseTransactions = transactions.filter { it.category.type.equals("expense", ignoreCase = true) }
        val incomeTransactions = transactions.filter { it.category.type.equals("income", ignoreCase = true) }

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

    private fun updateCategoryLists(categories: List<Category>) {
        val expenseCategories = categories.filter { it.type.equals("expense", ignoreCase = true) }
        val incomeCategories = categories.filter { it.type.equals("income", ignoreCase = true) }

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

    private fun showCategoryExpenseToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.VISIBLE
            recyclerViewIncomeCategory.visibility = View.GONE
            setupPieChart(isExpense = true)
            highlightToggle(btnCategoryExpenseToggle, btnCategoryIncomeToggle)
        }
    }

    private fun showCategoryIncomeToggle() {
        binding.apply {
            recyclerViewExpenseCategory.visibility = View.GONE
            recyclerViewIncomeCategory.visibility = View.VISIBLE
            setupPieChart(isExpense = false)
            highlightToggle(btnCategoryIncomeToggle, btnCategoryExpenseToggle)
        }
    }

    private fun highlightToggle(selected: LinearLayout, unselected: LinearLayout) {
        selected.setBackgroundResource(R.drawable.toggle_selected)
        unselected.setBackgroundResource(android.R.color.transparent)
    }

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

    private fun navigateToTransactionDetails(transaction: Transaction) {
        // TODO: Implement navigation to transaction details
    }

    private fun navigateToCategoryDetails(category: Category) {
        // TODO: Implement navigation to category details
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
