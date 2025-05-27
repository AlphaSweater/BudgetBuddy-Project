package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import dagger.hilt.android.AndroidEntryPoint

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
        highlightToggle(binding.btnCategoryExpenseToggle, binding.btnCategoryIncomeToggle)
    }

    private fun showCategoryIncomeToggle() {
        binding.recyclerViewExpenseCategory.visibility = View.GONE
        binding.recyclerViewIncomeCategory.visibility = View.VISIBLE
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

    private fun setupLineChart() {
        val lineChart: LineChart = binding.lineChart
        val context = lineChart.context

        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        val incomeValues = listOf(3000f, 3200f, 3100f, 4000f, 4200f, 4100f)
        val expenseValues = listOf(1500f, 1800f, 1600f, 2000f, 2300f, 2200f)

        val incomeEntries = incomeValues.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }
        val expenseEntries = expenseValues.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
            color = ContextCompat.getColor(context, R.color.profit_green)
            valueTextColor = R.attr.bb_primaryText
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(context, R.color.profit_green))
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER

            // Gradient fill
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_income)
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Expense").apply {
            color = ContextCompat.getColor(context, R.color.expense_red)
            valueTextColor = R.attr.bb_primaryText
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(context, R.color.expense_red))
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER

            // Gradient fill
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_expense)
        }

        val lineData = LineData(incomeDataSet, expenseDataSet)
        lineChart.data = lineData

        // X-axis
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(months)
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelRotationAngle = -45f
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "Income vs Expense (6 Months)"
        lineChart.animateX(1000)
        lineChart.invalidate()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
