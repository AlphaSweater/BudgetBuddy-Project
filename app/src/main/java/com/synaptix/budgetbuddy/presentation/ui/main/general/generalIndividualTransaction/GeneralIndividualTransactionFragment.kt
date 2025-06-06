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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentGeneralIndividualTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.main.home.HomeMainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class GeneralIndividualTransactionFragment : Fragment() {

    private var _binding: FragmentGeneralIndividualTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralIndividualTransactionViewModel by viewModels()
    private val homeViewModel: HomeMainViewModel by activityViewModels()
    private val args: GeneralIndividualTransactionFragmentArgs by navArgs()

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
        setupViews()
        observeTransaction()
    }

    private fun setupViews() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeTransaction() {
        viewModel.selectedTransaction.observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                // Update top bar
                binding.individualCategoryName.text = it.category.name
                binding.amount.text = "R ${String.format("%.2f", it.amount)}"

                // Update wallet row
                binding.rowSelectWallet.findViewById<TextView>(R.id.walletName)?.text = 
                    it.wallet.name

                // Update amount row
                binding.rowAmountRow.findViewById<TextView>(R.id.textAmount)?.text = 
                    "R ${String.format("%.2f", it.amount)}"

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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}