package com.synaptix.budgetbuddy.presentation.ui.main.general.generalIndividualTransaction

import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentGeneralIndividualTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.main.home.HomeMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class GeneralIndividualTransactionFragment : Fragment() {

    private var _binding: FragmentGeneralIndividualTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralIndividualTransactionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralIndividualTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TransactionFragment", "onViewCreated")
        setupViews()
        observeTransaction()
    }

    private fun setupViews() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeTransaction() {
        Log.d("TransactionFragment", "Starting to observe transaction")
        viewModel.selectedTransaction.observe(viewLifecycleOwner) { transaction ->
            Log.d("TransactionFragment", "Received transaction: ${transaction?.id}")
            transaction?.let {
                // Update top bar with color and sign based on category type
                val amountText = when (it.category.type.lowercase()) {
                    "expense" -> {
                        binding.amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red))
                        "-R ${String.format("%.2f", it.amount)}"
                    }
                    "income" -> {
                        binding.amount.setTextColor(ContextCompat.getColor(requireContext(), R.color.profit_green))
                        "+R ${String.format("%.2f", it.amount)}"
                    }
                    else -> "R ${String.format("%.2f", it.amount)}"
                }
                binding.amount.text = amountText

                // Update wallet row
                binding.rowSelectWallet.findViewById<TextView>(R.id.walletName)?.text = 
                    it.wallet.name

                // Update category row
                binding.individualCategoryName.text =
                    it.category.name

                // Update note row
                binding.rowNoteRow.findViewById<TextView>(R.id.textNote)?.text = 
                    it.note.ifEmpty { "No note provided" }

                // Update date row
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.rowStartRow.findViewById<TextView>(R.id.textStartDate)?.text = 
                    dateFormat.format(Date(it.date))

                // Update recurrence row
                binding.rowRecurrenceRow.findViewById<TextView>(R.id.textRecurrence)?.text = 
                    it.recurrenceData.toDisplayString()

                // Update photo if available
                if (it.photoUrl != null) {
                    // TODO: Load image from photoUrl using an image loading library
                    binding.imageView3.visibility = View.VISIBLE
                } else {
                    binding.imageView3.visibility = View.GONE
                }
            } ?: run {
                Log.d("TransactionFragment", "No transaction found, navigating back")
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}