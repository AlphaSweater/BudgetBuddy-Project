package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import android.app.DatePickerDialog
import android.icu.util.Calendar
import com.synaptix.budgetbuddy.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelPopupBottomSheet.LabelBottomSheet
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

        //spinner (dropbox) set up
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, 
            listOf("Currency", "USD", "EUR", "GBP")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnCurrency.adapter = adapter

        // Show bottom sheet when recurrence button is clicked
        binding.btnRecurrence.setOnClickListener {
            RecurrenceBottomSheet().show(parentFragmentManager, "RecurrenceBottomSheet")
        }

        // show bottom sheet when label button is clicked
        binding.btnLabel.setOnClickListener {
            LabelBottomSheet().show(parentFragmentManager, "LabelBottomSheet")
        }

        //Direct to another fragment AddTransactionCat
        binding.btnSelectCat.setOnClickListener {

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

        binding.btnDateSelect.setOnClickListener {
            openDatePicker()
        }

        binding.edtTextDate.setOnClickListener {
            openDatePicker()
        }

        binding.btnSave.setOnClickListener {
            val category = "Test Category" // Replace with actual category
            val walletId = "Test Wallet" // Replace with actual wallet
            val currencyType = "Test Currency" // Replace with actual currency type;
            val amount = 200.00 // Replace with actual amount [[  amountText.toDoubleOrNull()  ]]
            val date = "28/04/2025" // Replace with actual date
            val note = "Test Note" // Replace with actual note
            val labels = listOf("Label1", "Label2") // Replace with actual labels
            val photo = "Test Photo" // Replace with actual photo
            val recurrenceRate = "Weekly" // Replace with actual recurrence rate

            // Validation
            if (category.isBlank() || walletId.isBlank() || currencyType.isBlank() || amount <= 0.0 || date.toString().isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all required fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addTransaction(category, walletId, currencyType, amount, date, note, labels, photo, recurrenceRate)
            Toast.makeText(requireContext(), "Transaction saved successfully!", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}