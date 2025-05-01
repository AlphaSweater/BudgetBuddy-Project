package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
