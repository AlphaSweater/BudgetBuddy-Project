package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneralReportsFragment : Fragment() {

    private var _binding: FragmentGeneralReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralReportsViewModel by viewModels()
    private lateinit var generalReportsAdapter: GeneralReportAdapter

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
        viewModel.reportCategoryItems.observe(viewLifecycleOwner) { items ->
            binding.recyclerViewExpenseCategory.adapter = GeneralReportAdapter(items)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
