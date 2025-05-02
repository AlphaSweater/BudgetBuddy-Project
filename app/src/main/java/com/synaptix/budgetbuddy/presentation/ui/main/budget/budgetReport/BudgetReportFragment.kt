package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.databinding.FragmentBudgetReportBinding

class BudgetReportFragment : Fragment() {

    private var _binding: FragmentBudgetReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetReportViewModel by viewModels()

    private lateinit var budgetReportAdapter: BudgetReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
    }

    private fun setupRecyclers() {
        transactionRecycler()
        categoryRecycler()
    }

    private fun transactionRecycler() {
        // Code for the transaction recycler
        val items = listOf(
            BudgetReportListItems.DateHeader("1", "Monday", "May 2025", -2000.00),
            BudgetReportListItems.TransactionItem("Lunchhh", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
            BudgetReportListItems.TransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
            BudgetReportListItems.DateHeader("1", "Monday", "April 2025", -4000.00),
            BudgetReportListItems.TransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
            BudgetReportListItems.TransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
        )

        budgetReportAdapter = BudgetReportAdapter(items)

        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetReportAdapter
        }
    }

    private fun categoryRecycler(){
        val categoryItems = listOf(
            BudgetReportListItems.CategoryItems(
                categoryName = "Food",
                categoryIcon = R.drawable.baseline_fastfood_24,
                categoryColour = R.color.cat_light_blue,
                transactionCount = 5,
                amount = "R 1,000",
                relativeDate = "This Week"
            ),
            BudgetReportListItems.CategoryItems(
                categoryName = "Transport",
                categoryIcon = R.drawable.ic_car_24,
                categoryColour = R.color.cat_orange,
                transactionCount = 2,
                amount = "R 500",
                relativeDate = "Yesterday"
            ),
            BudgetReportListItems.CategoryItems(
                categoryName = "Entertainment",
                categoryIcon = R.drawable.baseline_music_note_24,
                categoryColour = R.color.cat_dark_green,
                transactionCount = 3,
                amount = "R 750",
                relativeDate = "Last Month"
            )
        )

        // New adapter instance for category items
        val categoryAdapter = BudgetReportAdapter(categoryItems)

        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
