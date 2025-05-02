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
//        binding.rowRecurrence.setOnClickListener {
//            // Navigate to Recurrence Fragment instead of BottomSheet
//            findNavController().navigate(R.id.action_budgetAddFragment_to_transactionSelectRecurrenceFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
