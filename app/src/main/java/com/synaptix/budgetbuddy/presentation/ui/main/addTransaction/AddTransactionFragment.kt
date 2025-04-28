package com.synaptix.budgetbuddy.ui.addTransaction

import android.app.DatePickerDialog
import android.icu.util.Calendar
import com.synaptix.budgetbuddy.R
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding
import com.synaptix.budgetbuddy.ui.addTransactionCat.AddTransactionCatFragment
import com.synaptix.budgetbuddy.ui.labelPopupBottomSheet.LabelBottomSheet
import com.synaptix.budgetbuddy.ui.recurrence.RecurrenceBottomSheet
import androidx.navigation.fragment.findNavController

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

        //Direct to another fragment AddTransactionCat
        binding.btnSelectCat.setOnClickListener {

        }

        val editTextDate = view.findViewById<EditText>(R.id.editTextDate)

        // Set an OnClickListener to open the DatePickerDialog when the EditText is clicked
        editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Create and show DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.CustomDatePickerDialog, // Use custom style here
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date and set it in the EditText
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    editTextDate.setText(formattedDate)
                },
                year, month, day
            )

            datePickerDialog.show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}