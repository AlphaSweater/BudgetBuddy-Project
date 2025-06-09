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
import android.text.Editable
import android.text.TextWatcher
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
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.databinding.FragmentBudgetAddBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@AndroidEntryPoint
class BudgetAddFragment : Fragment() {

    private var _binding: FragmentBudgetAddBinding? = null
    private val binding get() = _binding!!
    
    private val sharedViewModel: BudgetAddViewModel by navGraphViewModels(R.id.ind_budget_navigation_graph) { defaultViewModelProviderFactory }

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
        setupTextWatchers()
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
        // Get the raw amount from the ViewModel or formatted text
        val amountText = binding.amount.text.toString()
        val amount = amountText.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()

        sharedViewModel.setBudgetName(binding.budgetName.text.toString())
        sharedViewModel.setBudgetAmount(amount)
        sharedViewModel.showValidationErrors()

        if (sharedViewModel.validateForm()) {
            sharedViewModel.addBudget()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.uiState.collectLatest { state ->
                        handleUiState(state)
                    }
                }
                launch {
                    sharedViewModel.validationState.collectLatest { state ->
                        handleValidationState(state)
                    }
                }
                launch {
                    sharedViewModel.selectedCategories.collectLatest { categories ->
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
                sharedViewModel.reset()
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
        } else {
            // Get all categories from the ViewModel to check if all are selected
            val allCategories = sharedViewModel.getAllCategories()
            val isAllSelected = allCategories.isNotEmpty() && categories.size == allCategories.size

            if (isAllSelected) {
                binding.textSelectedCategoryName.text = "All Categories selected"
            } else {
                // For individual selections, show a summary
                val summary = when {
                    categories.size == 1 -> categories.first().name
                    categories.size == 2 -> "${categories[0].name} & ${categories[1].name}"
                    categories.size > 2 -> "${categories.size} Categories selected"
                    else -> "No categories selected"
                }
                binding.textSelectedCategoryName.text = summary
            }
        }
    }

    private fun setupTextWatchers() {
        setupAmountWatcher()
    }

    private fun setupAmountWatcher() {
        binding.amount.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true
                try {
                    if (s.toString() != current) {
                        binding.amount.removeTextChangedListener(this)

                        // Remove all non-digit characters
                        val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                        // Handle empty input
                        if (cleanString.isEmpty()) {
                            current = ""
                            binding.amount.setText("")
                            sharedViewModel.setBudgetAmount(0.0)
                            binding.amount.addTextChangedListener(this)
                            return
                        }

                        // Limit to 14 digits
                        val limitedString = if (cleanString.length > 14) {
                            cleanString.substring(0, 14)
                        } else {
                            cleanString
                        }

                        // Convert to double and format with 2 decimal places
                        val amount = limitedString.toDoubleOrNull()?.div(100) ?: 0.0
                        val formatted = String.format("%,.2f", amount)

                        current = formatted
                        binding.amount.setText(formatted)
                        binding.amount.setSelection(formatted.length)

                        // Store the raw amount in the ViewModel
                        sharedViewModel.setBudgetAmount(amount)

                        binding.amount.addTextChangedListener(this)
                    }
                } finally {
                    isFormatting = false
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\