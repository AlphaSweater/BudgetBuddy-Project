package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentBudgetAddBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectRecurrencePopUp.TransactionSelectRecurrenceBottomSheet

class BudgetAddFragment : Fragment() {

    private var _binding: FragmentBudgetAddBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = BudgetAddFragment()
    }

    private val viewModel: BudgetAddViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_budget_add, container, false)

        setupCurrencySpinner()
        setupClickListeners()
    }


    private fun setupCurrencySpinner() {
        val currencies = arrayOf("ZAR", "USD", "EUR", "GBP")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.rowRecurrence.setOnClickListener {
            TransactionSelectRecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}