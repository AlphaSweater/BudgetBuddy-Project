package com.synaptix.budgetbuddy.presentation.ui.main.budget

import android.graphics.Typeface
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
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.synaptix.budgetbuddy.core.usecase.main.display.BudgetSummary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
        setupClickListeners()
        collectUiState()
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
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.budgetUiState.collect { state ->
                        when (state) {
                            is BudgetMainViewModel.BudgetUiState.Loading -> {
                                // Show loading if needed
                            }
                            is BudgetMainViewModel.BudgetUiState.Success -> {
                                val budgetItems = state.budgets.map { budget ->
                                    BudgetListItems.BudgetBudgetItem(
                                        budget = budget,
                                        status = "R ${budget.spent} spent of R ${budget.amount}"
                                    )
                                }
                                budgetAdapter.submitList(budgetItems)
                            }
                            is BudgetMainViewModel.BudgetUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                            is BudgetMainViewModel.BudgetUiState.Empty -> {
                                budgetAdapter.submitList(emptyList())
                            }
                        }
                    }
                }

                launch {
                    viewModel.budgetSummary.collect { summary ->
                        summary?.let { updateSummaryUI(it) }
                    }
                }
            }
        }
    }

    private fun updateSummaryUI(summary: BudgetSummary) {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.bb_primaryText, typedValue, true)
        val primaryColor = typedValue.data

        val greenColor = ContextCompat.getColor(requireContext(), R.color.profit_green)
        val blueColor = ContextCompat.getColor(requireContext(), R.color.info_blue)

        val currencyFormat = NumberFormat.getNumberInstance(Locale("en", "ZA")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

        val formattedBudgeted = currencyFormat.format(summary.totalBudgeted)
        val formattedSpent = currencyFormat.format(summary.totalSpent)

        val budgetedText = SpannableString("ZAR $formattedBudgeted").apply {
            setSpan(ForegroundColorSpan(primaryColor), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(greenColor), 4, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 4, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val spentText = SpannableString("ZAR $formattedSpent").apply {
            setSpan(ForegroundColorSpan(primaryColor), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(blueColor), 4, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 4, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.totalBudgetedTextView.text = budgetedText
        binding.totalSpentTextView.text = spentText
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