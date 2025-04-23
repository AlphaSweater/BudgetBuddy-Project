package com.synaptix.budgetbuddy.ui.addTransaction

import com.synaptix.budgetbuddy.R
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
import com.synaptix.budgetbuddy.ui.labelPopupBottomSheet.LabelBottomSheet
import com.synaptix.budgetbuddy.ui.recurrence.RecurrenceBottomSheet

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    //private val viewModel: AddTransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //spinner (dropbox) set up
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, 
            listOf("Currency", "USD", "EUR", "GBP")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner2.adapter = adapter

        // Show bottom sheet when recurrence button is clicked
        binding.btnRecurrence.setOnClickListener {
            RecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }

        // show bottom sheet when label button is clicked
        binding.btnLabel.setOnClickListener {
            LabelBottomSheet().show(parentFragmentManager, "LabelBottomSheet")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}