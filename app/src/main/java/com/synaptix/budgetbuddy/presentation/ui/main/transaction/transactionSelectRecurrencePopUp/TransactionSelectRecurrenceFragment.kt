package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectRecurrencePopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectRecurrenceBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import kotlin.getValue

class TransactionSelectRecurrenceFragment : Fragment() {

    private var _binding: FragmentTransactionSelectRecurrenceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()

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
        binding.btnDaily.setOnClickListener {
            viewModel.setRecurrenceRate("Daily")
            findNavController().popBackStack()
        }

        binding.btnWeekly.setOnClickListener {
            viewModel.setRecurrenceRate("Weekly")
            findNavController().popBackStack()
        }

        binding.btnMonthly.setOnClickListener {
            viewModel.setRecurrenceRate("Monthly")
            findNavController().popBackStack()
        }

        binding.btnYearly.setOnClickListener {
            viewModel.setRecurrenceRate("Yearly")
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
