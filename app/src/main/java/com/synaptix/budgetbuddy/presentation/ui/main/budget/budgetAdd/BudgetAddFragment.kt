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

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.databinding.FragmentBudgetAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetAddFragment : Fragment() {

    private var _binding: FragmentBudgetAddBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetAddViewModel by activityViewModels()

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectUiState()
                collectValidationState()
                collectSelectedCategories()
            }
        }
    }

    private fun setupUI() {
        setupCurrencySpinner()

        binding.btnSave.setOnClickListener {
            viewModel.setBudgetName(binding.budgetName.text.toString())
            viewModel.setBudgetAmount(binding.amount.text.toString().toDoubleOrNull())
            viewModel.showValidationErrors()

            if (viewModel.validateForm()) {
                viewModel.addBudget()
            }
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rowSelectCategory.setOnClickListener {
            findNavController().navigate(R.id.action_budgetAddFragment_to_budgetSelectCategoryFragment)
        }
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("ZAR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter
    }

    private suspend fun collectUiState() {
        viewModel.uiState.collectLatest { state ->
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
    }

    private suspend fun collectValidationState() {
        viewModel.validationState.collectLatest { state ->
            binding.textSelectedCategoryName.error = state.categoryError
        }
    }

    private suspend fun collectSelectedCategories() {
        viewModel.selectedCategories.collectLatest { selected ->
            binding.textSelectedCategoryName.text =
                if (selected.isEmpty()) "No categories selected"
                else selected.joinToString(", ") { it.name }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\