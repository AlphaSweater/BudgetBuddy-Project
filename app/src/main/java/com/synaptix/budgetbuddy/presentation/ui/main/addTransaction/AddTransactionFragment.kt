package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import android.app.DatePickerDialog
import android.icu.util.Calendar
import com.synaptix.budgetbuddy.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelectorDialog.LabelSelectorDialogFragment
import com.synaptix.budgetbuddy.ui.recurrence.RecurrenceBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()

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
    }

    private fun setupUI() {
        //spinner (dropbox) set up
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("Currency", "USD", "EUR", "GBP")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        // Show bottom sheet when recurrence button is clicked
        binding.rowSelectRecurrenceRate.setOnClickListener {
            RecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }

        // show bottom sheet when label button is clicked
        binding.rowSelectLabel.setOnClickListener {
            showLabelSelector() // <- we’ll make this next
        }

        //Direct to another fragment AddTransactionCat
        binding.rowSelectCategory.setOnClickListener {

        }

        val openDatePicker = {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.CustomDatePickerDialog,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.edtTextDate.setText(formattedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.rowSelectDate.setOnClickListener {
            openDatePicker()
        }

        binding.edtTextDate.setOnClickListener {
            openDatePicker()
        }

        binding.btnSave.setOnClickListener {
            val category = "Test Category" // Replace with actual category
            viewModel.category.value = category;

            val walletId = "Test Wallet" // Replace with actual wallet
            viewModel.walletId.value  = walletId;

            val currencyType = "Test Currency" // Replace with actual currency type;
            viewModel.currency.value  = currencyType;

            val amount = binding.edtTextAmount.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.amount.value = amount;

            val date = binding.edtTextDate.text.toString()
            viewModel.date.value  = date;

            val note = binding.edtTextNote.text.toString()
            viewModel.note.value  = note;

            val labels = listOf("Label1", "Label2") // Replace with actual labels
            viewModel.labels.value  = labels;

            val photo = "Test Photo" // Replace with actual photo
            viewModel.photo.value  = photo;

            val recurrenceRate = "Weekly" // Replace with actual recurrence rate
            viewModel.recurrenceRate.value  = recurrenceRate;

            // Validation
            if (category.isBlank() || walletId.isBlank() || currencyType.isBlank() || amount <= 0.0 || date.toString().isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all required fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addTransaction()
            Toast.makeText(requireContext(), "Transaction saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLabelSelector() {
        val currentLabels = viewModel.labels.value ?: emptyList()
        val dialog = LabelSelectorDialogFragment.newInstance(currentLabels)

        dialog.setOnLabelsSelected { selected ->
            viewModel.labels.value = selected
        }

        dialog.show(parentFragmentManager, "LabelSelector")
    }


    private fun observeViewModel() {
        viewModel.labels.observe(viewLifecycleOwner) { selectedLabels ->
            // TODO: show chips or comma-separated labels somewhere
            Log.d("Labels", selectedLabels.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}