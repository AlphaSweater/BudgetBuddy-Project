package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.synaptix.budgetbuddy.databinding.FragmentBudgetAddBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectRecurrencePopUp.TransactionSelectRecurrenceFragment
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R

class BudgetAddFragment : Fragment() {

    private var _binding: FragmentBudgetAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetAddViewModel by viewModels()

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
            // Update ViewModel LiveData
            viewModel.budgetName.value = binding.budgetName.text.toString()
            viewModel.budgetAmount.value = binding.amount.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.walletId.value = 1

            // Call function to save budget
            viewModel.addBudget()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
