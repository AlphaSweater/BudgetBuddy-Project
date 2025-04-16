package com.synaptix.budgetbuddy.ui.addTransaction

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
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

        // Show bottom sheet when recurrence button is clicked
        binding.btnReccurrenceDialog.setOnClickListener {
            RecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }

        // Observe the selected recurrence from ViewModel and update label
        //viewModel.recurrenceOption.observe(viewLifecycleOwner) { selection ->
        //    binding.recurrenceLabel.text = selection
        //}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}