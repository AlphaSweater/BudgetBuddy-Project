package com.synaptix.budgetbuddy.presentation.ui.main.budget

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
import com.synaptix.budgetbuddy.databinding.FragmentBudgetMainBinding
import com.synaptix.budgetbuddy.databinding.FragmentGeneralTransactionsBinding
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions.GeneralTransactionsAdapter

class BudgetMainFragment : Fragment() {

    private var _binding: FragmentBudgetMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetMainViewModel by viewModels()
    private lateinit var budgetMainAdapter: BudgetMainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
    }

    private fun setupRecyclers() {
        recyclerViewBudgetMain()
    }
    private fun recyclerViewBudgetMain() {
        val budgetItems = listOf(
            BudgetReportListItems.BudgetItem(
                title = "Groceries",
                status = "R1,200 spent of R2,000",
                categoryIcon = R.drawable.baseline_shopping_bag_24),
            BudgetReportListItems.BudgetItem(
                title = "Transport",
                status = "R300 spent of R1,000",
                categoryIcon = R.drawable.ic_car_24),
            BudgetReportListItems.BudgetItem(
                title = "Eating Out",
                status = "R850 spent of R900",
                categoryIcon = R.drawable.baseline_fastfood_24),
            BudgetReportListItems.BudgetItem(
                title = "Subscriptions",
                status = "R500 spent of R600",
                categoryIcon = R.drawable.baseline_computer_24)
        )

        budgetMainAdapter = BudgetMainAdapter(budgetItems) { item ->
            findNavController().navigate(R.id.action_budgetMainFragment_to_budgetReportFragment)
        }

        binding.recyclerViewBudgetMain.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetMainAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}