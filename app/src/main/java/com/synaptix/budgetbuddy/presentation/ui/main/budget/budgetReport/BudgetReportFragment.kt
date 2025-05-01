package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.core.model.ListItem
import com.synaptix.budgetbuddy.databinding.FragmentBudgetReportBinding

class BudgetReportFragment : Fragment() {

    //binding. Pretty straight forward
    private var _binding: FragmentBudgetReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetReportViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    //This is the test data that uses the adapter and the ItemList core thingy -- this might be the issue but to be honest I am so cooked rn
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        budgetAdapter = BudgetAdapter()

        val itemList = listOf(
            ListItem.TransactionItem("Today", "18", "April 2025", "-R200"),
            ListItem.CategoryItem("Groceries", "5 transactions", "-R550", "Last Saturday")
        )

        // hmmm binding
        binding.recyclerViewBudgetReport.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }

        budgetAdapter.submitList(itemList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
