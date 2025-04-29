package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelector.Label
import com.synaptix.budgetbuddy.ui.recurrence.RecurrenceBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()

    // --- Lifecycle ---
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
        setupUI()
        observeViewModel()
        setupFragmentResultListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setup Methods ---
    private fun setupUI() {
        setupCurrencySpinner()
        setupClickListeners()
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("Currency", "USD", "EUR", "GBP")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.rowSelectRecurrenceRate.setOnClickListener {
            RecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }

        binding.rowSelectLabel.setOnClickListener {
            showLabelSelector()
        }

        binding.rowSelectCategory.setOnClickListener {
            // TODO: Navigate to category selector
        }

        val openDatePicker = {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.CustomDatePickerDialog,
                { _, year, month, dayOfMonth ->
                    val formattedDate = "$dayOfMonth/${month + 1}/$year"
                    binding.edtTextDate.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.rowSelectDate.setOnClickListener { openDatePicker() }
        binding.edtTextDate.setOnClickListener { openDatePicker() }

        binding.btnSave.setOnClickListener { saveTransaction() }

        binding.btnGoBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "labelSelectorResult",
            viewLifecycleOwner
        ) { _, bundle ->
            @Suppress("DEPRECATION")
            val selectedLabels = bundle.getSerializable("selectedLabels") as? ArrayList<Label> ?: arrayListOf()
            viewModel.labels.value = selectedLabels
        }
    }

    private fun updateSelectedLabelChips(labels: List<Label>) {
        val selectedLabels = labels.filter { it.isSelected }

        val chipGroup = binding.chipGroupLabels
        chipGroup.removeAllViews()

        if (selectedLabels.isEmpty()) {
            chipGroup.visibility = View.GONE
            return
        }

        chipGroup.visibility = View.VISIBLE

        selectedLabels.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label.labelName
                isClickable = false
                isCheckable = false

                // Resolve the color from the theme attribute
                val chipBackgroundColor = MaterialColors.getColor(this.context, R.attr.bb_surfaceAlt, Color.TRANSPARENT)
                setChipBackgroundColor(ColorStateList.valueOf(chipBackgroundColor))

                val textColor = MaterialColors.getColor(this.context, R.attr.bb_primaryText, Color.TRANSPARENT)
                setTextColor(textColor)
            }
            chipGroup.addView(chip)
        }
    }


    // --- Save Logic ---
    private fun saveTransaction() {
        // TODO: Replace with actual data from UI
        viewModel.category.value = "Test Category"
        viewModel.walletId.value = "Test Wallet"
        viewModel.currency.value = "Test Currency"

        val amount = binding.edtTextAmount.text.toString().toDoubleOrNull() ?: 0.0
        viewModel.amount.value = amount

        val date = binding.edtTextDate.text.toString()
        viewModel.date.value = date

        viewModel.note.value = binding.edtTextNote.text.toString()
        viewModel.photo.value = "Test Photo" // Replace with actual photo
        viewModel.recurrenceRate.value = "Weekly" // Replace with actual rate

        // Validate input
        if (viewModel.category.value.isNullOrBlank() ||
            viewModel.walletId.value.isNullOrBlank() ||
            viewModel.currency.value.isNullOrBlank() ||
            amount <= 0.0 ||
            date.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Please fill in all required fields correctly.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.addTransaction()
        Toast.makeText(requireContext(), "Transaction saved successfully!", Toast.LENGTH_SHORT).show()
    }

    // --- Label Navigation ---
    private fun showLabelSelector() {
        val currentLabels = viewModel.labels.value ?: emptyList()
        val bundle = Bundle().apply {
            putSerializable("currentLabels", ArrayList(viewModel.labels.value))
        }
        findNavController().navigate(
            R.id.action_addTransactionFragment_to_labelSelectorFragment,
            bundle
        )
    }

    // --- Observers ---
    private fun observeViewModel() {
        viewModel.labels.observe(viewLifecycleOwner) { selectedLabels ->
            Log.d("Labels", selectedLabels.toString())
            updateSelectedLabelChips(viewModel.labels.value?: emptyList())
            // TODO: Show labels as chips or in a list
        }
    }
}