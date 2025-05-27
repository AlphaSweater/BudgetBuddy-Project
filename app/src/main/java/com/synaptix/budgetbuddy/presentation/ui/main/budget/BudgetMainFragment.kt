package com.synaptix.budgetbuddy.presentation.ui.main.budget

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.databinding.FragmentBudgetMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetMainFragment : Fragment() {

    private var _binding: FragmentBudgetMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetMainViewModel by viewModels()
    private val budgetAdapter by lazy {
        BudgetMainAdapter { budget ->
            onBudgetClicked(budget)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.fetchBudgets()
        viewModel.fetchBudgetSummary()
    }

    private fun setupUI() {
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewBudgetMain.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }
    }

    private fun setupClickListeners() {
        binding.createBudgetButton.setOnClickListener {
            findNavController().navigate(R.id.action_budgetMainFragment_to_budgetAddFragment)
        }
        
        binding.saveButton.setOnClickListener {
            val minGoalText = binding.inputMinGoal.text.toString()
            val maxGoalText = binding.inputMaxGoal.text.toString()

            val minGoal = minGoalText.toDoubleOrNull()
            val maxGoal = maxGoalText.toDoubleOrNull()

            if (minGoal == null || maxGoal == null) {
                Toast.makeText(
                    requireContext(),
                    "Please enter valid goal values.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // TODO: Save the min and max goals to the ViewModel or database
//            viewModel.saveMinMaxGoals(minGoal, maxGoal)
        }
    }

    private fun observeViewModel() {
//        viewModel.minMaxGoal.observe(viewLifecycleOwner) { minMaxGoal ->
//            if (minMaxGoal != null) {
//                binding.minValueSpent.text = "R%.2f".format(minMaxGoal.minGoal)
//                binding.minValueTotal.text = "R%.2f".format(minMaxGoal.maxGoal)
//            } else {
//                binding.minValueSpent.text = "N/A"
//                binding.minValueTotal.text = "N/A"
//            }
//        }

        viewModel.budgets.observe(viewLifecycleOwner) { budgetList ->
            val budgetItems = budgetList.map { budget ->
                BudgetListItems.BudgetBudgetItem(
                    budget = budget,
                    status = "R ${budget.spent} spent of R ${budget.amount}"
                )
            }
            budgetAdapter.submitList(budgetItems)
        }

        viewModel.budgetSummary.observe(viewLifecycleOwner) { summary ->
            binding.totalBudgetedTextView.text = "ZAR %.2f".format(summary.totalBudgeted)
            binding.totalSpentTextView.text = "ZAR %.2f".format(summary.totalSpent)
        }

    }

    private fun onBudgetClicked(budget: Budget) {
        // TODO: Navigate to budget report or details
        findNavController().navigate(R.id.action_budgetMainFragment_to_budgetReportFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\