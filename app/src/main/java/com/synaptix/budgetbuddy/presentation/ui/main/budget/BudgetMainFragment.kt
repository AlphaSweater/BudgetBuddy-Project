package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.databinding.FragmentBudgetMainBinding
import com.synaptix.budgetbuddy.databinding.FragmentGeneralTransactionsBinding
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions.GeneralTransactionsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
        viewModel.fetchBudgets()

        viewModel.budgets.observe(viewLifecycleOwner) { budgetList ->
            setupRecyclerView(budgetList)
        }
        setupOnClickListeners()
    }

    private fun setupRecyclerView(budgetList: List<Budget>) {

        val budgetItems = budgetList.map { budget ->
            BudgetReportListItems.BudgetItem(
                id = budget.budgetId,
                title = budget.budgetName,
                status = "R ${budget.spent} spent of R ${budget.amount}",
                categoryIcon = R.drawable.baseline_shopping_bag_24
            )
        }

        budgetMainAdapter = BudgetMainAdapter(budgetItems) { item ->
//            findNavController().navigate(R.id.action_budgetMainFragment_to_budgetReportFragment)
        }

        binding.recyclerViewBudgetMain.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetMainAdapter
        }
    }

    private fun setupOnClickListeners() {
        binding.createBudgetButton.setOnClickListener {
            findNavController().navigate(R.id.action_budgetMainFragment_to_budgetAddFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}