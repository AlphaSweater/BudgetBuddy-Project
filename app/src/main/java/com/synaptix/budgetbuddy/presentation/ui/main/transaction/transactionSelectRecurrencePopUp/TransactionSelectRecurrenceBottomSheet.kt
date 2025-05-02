package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectRecurrencePopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectRecurrenceBinding

class TransactionSelectRecurrenceFragment : Fragment() {

    private var _binding: FragmentTransactionSelectRecurrenceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectRecurrenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        binding.optionEveryDay.setOnClickListener {
            // Handle "Every day" click
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.optionEveryWeek.setOnClickListener {
            // Handle "Every week" click
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.optionEveryWeek2.setOnClickListener {
            // Handle "Every 2 weeks" click
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
