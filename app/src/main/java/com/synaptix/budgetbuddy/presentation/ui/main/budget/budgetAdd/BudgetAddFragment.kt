//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.databinding.FragmentBudgetAddBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetAddFragment : Fragment() {

    private var _binding: FragmentBudgetAddBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: BudgetAddViewModel by navGraphViewModels(R.id.ind_budget_navigation_graph) { defaultViewModelProviderFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setupCurrencySpinner()
        setupClickListeners()
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("ZAR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveBudget()
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rowSelectCategory.setOnClickListener {
            findNavController().navigate(R.id.action_budgetAddFragment_to_budgetSelectCategoryFragment)
        }

    }

    private fun saveBudget() {
        viewModel.setBudgetName(binding.budgetName.text.toString())
        viewModel.setBudgetAmount(binding.amount.text.toString().toDoubleOrNull())
        viewModel.showValidationErrors()

        if (viewModel.validateForm()) {
            viewModel.addBudget()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        handleUiState(state)
                    }
                }
                launch {
                    viewModel.validationState.collectLatest { state ->
                        handleValidationState(state)
                    }
                }
                launch {
                    viewModel.selectedCategories.collectLatest { categories ->
                        updateSelectedCategories(categories)
                    }
                }
            }
        }
    }

    private fun handleUiState(state: BudgetAddViewModel.UiState) {
        when (state) {
            is BudgetAddViewModel.UiState.Loading -> {
                binding.btnSave.isEnabled = false
            }
            is BudgetAddViewModel.UiState.Success -> {
                Toast.makeText(requireContext(), "Budget added successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                viewModel.reset()
            }
            is BudgetAddViewModel.UiState.Error -> {
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun handleValidationState(state: BudgetAddViewModel.ValidationState) {
        binding.textNameError.apply {
            text = state.nameError
            visibility = if (state.shouldShowErrors && state.nameError != null) View.VISIBLE else View.GONE
        }

        binding.textCategoryError.apply {
            text = state.categoryError
            visibility = if (state.shouldShowErrors && state.categoryError != null) View.VISIBLE else View.GONE
        }

        binding.textAmountError.apply {
            text = state.amountError
            visibility = if (state.shouldShowErrors && state.amountError != null) View.VISIBLE else View.GONE
        }
    }

    private fun updateSelectedCategories(categories: List<Category>) {
        if (categories.isEmpty()) {
            binding.textSelectedCategoryName.text = "No categories selected"
            binding.imgSelectedCategoryIcon.setImageResource(R.drawable.ic_ui_categories)
            binding.imgSelectedCategoryIcon.setColorFilter(requireContext().getThemeColor(R.attr.bb_accent))
        } else {
            binding.textSelectedCategoryName.text = categories.joinToString(", ") { it.name }
            binding.imgSelectedCategoryIcon.setImageResource(categories.first().icon)
            binding.imgSelectedCategoryIcon.setColorFilter(requireContext().getColor(categories.first().color))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\