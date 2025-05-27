package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar

@AndroidEntryPoint
class GeneralReportsFragment : Fragment() {

    private var _binding: FragmentGeneralReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralReportsViewModel by viewModels()
//    private lateinit var generalReportsAdapter: GeneralReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        setupOnClickListeners()
        viewModel.transactions.observe(viewLifecycleOwner) { transactionList ->
            if (!transactionList.isNullOrEmpty()) {
                setupLineChart(transactionList)
            }
        }

        viewModel.loadTransactions()

        setupPieChart(isExpense = true)
//        viewModel.reportBudgetCategoryItem.observe(viewLifecycleOwner) { items ->
//            binding.recyclerViewExpenseCategory.adapter = GeneralReportAdapter(items)
//        }
    }

    private fun setupRecyclers() {
//        labelRecycler()
//        categoryRecycler()
    }

//    private fun labelRecycler() {
////        val labelItems = listOf(
////            BudgetReportListItems.LabelItems(
////                labelName = "Food",
////                labelIcon = R.drawable.baseline_fastfood_24,
////                labelColour = R.color.cat_light_blue,
////                transactionCount = 5,
////                amount = "R 1,000",
////                relativeDate = "This Week"
////            )
////        )
//
//        binding.recyclerViewExpenseLabel.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = GeneralReportAdapter(labelItems)
//        }
//    }

//    private fun categoryRecycler() {
////        val categoryItems = listOf(
////            BudgetReportListItems.CategoryItems(
////                categoryName = "Food",
////                categoryIcon = R.drawable.baseline_fastfood_24,
////                categoryColour = R.color.cat_light_blue,
////                transactionCount = 5,
////                amount = "R 1,000",
////                relativeDate = "This Week"
////            )
////        )
//
//        binding.recyclerViewExpenseCategory.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = GeneralReportAdapter(categoryItems)
//        }
//    }

    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCategoryExpenseToggle.setOnClickListener {
            showCategoryExpenseToggle()
        }

        binding.btnCategoryIncomeToggle.setOnClickListener {
            showCategoryIncomeToggle()
        }

        binding.btnLabelIncomeToggle.setOnClickListener {
            showLabelIncomeToggle()
        }

        binding.btnLabelExpenseToggle.setOnClickListener {
            showLabelExpenseToggle()
        }
    }

    private fun showCategoryExpenseToggle() {
        binding.recyclerViewExpenseCategory.visibility = View.VISIBLE
        binding.recyclerViewIncomeCategory.visibility = View.GONE

        setupPieChart(isExpense = true)

        highlightToggle(binding.btnCategoryExpenseToggle, binding.btnCategoryIncomeToggle)
    }

    private fun showCategoryIncomeToggle() {
        binding.recyclerViewExpenseCategory.visibility = View.GONE
        binding.recyclerViewIncomeCategory.visibility = View.VISIBLE

        setupPieChart(isExpense = false)

        highlightToggle(binding.btnCategoryIncomeToggle, binding.btnCategoryExpenseToggle)
    }

    private fun showLabelExpenseToggle() {
        binding.recyclerViewExpenseLabel.visibility = View.VISIBLE
        binding.recyclerViewIncomeLabel.visibility = View.GONE
        highlightToggle(binding.btnLabelExpenseToggle, binding.btnLabelIncomeToggle)
    }

    private fun showLabelIncomeToggle() {
        binding.recyclerViewExpenseLabel.visibility = View.GONE
        binding.recyclerViewIncomeLabel.visibility = View.VISIBLE
        highlightToggle(binding.btnLabelIncomeToggle, binding.btnLabelExpenseToggle)
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
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }

            axisRight.isEnabled = false
            description.text = "Income vs Expense (6 Months)"

            animateXY(1000, 1200, Easing.EaseInOutCubic)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            invalidate()
        }
    }


    private fun setupPieChart(isExpense: Boolean) {
        val context = binding.root.context
        val pieChart: PieChart = binding.pieChart

        val categories: List<String>
        val amounts: List<Float>
        val label: String

        if (isExpense) {
            categories = listOf("Food", "Transport", "Entertainment", "Bills", "Shopping")
            amounts = listOf(800f, 400f, 300f, 500f, 600f)
            label = "Expenses"
        } else {
            categories = listOf("Salary", "Freelance", "Investments", "Gift")
            amounts = listOf(1500f, 500f, 300f, 200f)
            label = "Income"
        }

        val pieEntries = categories.mapIndexed { index, category ->
            PieEntry(amounts[index], category)
        }

        val pieDataSet = PieDataSet(pieEntries, label).apply {
            colors = listOf(
                ContextCompat.getColor(context, R.color.cat_dark_green),
                ContextCompat.getColor(context, R.color.cat_light_pink),
                ContextCompat.getColor(context, R.color.cat_dark_blue),
                ContextCompat.getColor(context, R.color.cat_yellow),
                ContextCompat.getColor(context, R.color.cat_orange)
            )
            valueTextSize = 14f
            valueTextColor = ContextCompat.getColor(context, R.color.light_text)
        }

        val pieData = PieData(pieDataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.apply {
            data = pieData
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            centerText = label
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
