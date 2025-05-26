package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectRecurrencePopUp

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectRecurrenceBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TransactionSelectRecurrenceFragment : Fragment() {

    private var _binding: FragmentTransactionSelectRecurrenceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()
    private val recurrenceViewModel: TransactionSelectRecurrenceViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

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
        setupInitialState()
        setupClickListeners()
        observeViewModel()
        restorePreviousState()
    }

    private fun setupInitialState() {
        binding.btnOnceOff.isChecked = true
        binding.recurrenceDetailsCard.isVisible = false
        binding.endDateCard.isVisible = false
        
        // Set all week day chips to checked by default
        listOf(
            binding.chipMon,
            binding.chipTue,
            binding.chipWed,
            binding.chipThu,
            binding.chipFri,
            binding.chipSat,
            binding.chipSun
        ).forEach { chip ->
            chip.isChecked = true
            val color = requireContext().getThemeColor(R.attr.bb_buttonSelected)
            chip.chipBackgroundColor = ColorStateList.valueOf(color)
        }
    }

    private fun setupClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveRecurrence()
        }

        // Recurrence type buttons
        binding.btnOnceOff.setOnClickListener { updateRecurrenceTypeUI(TransactionSelectRecurrenceViewModel.RecurrenceType.ONCE_OFF) }
        binding.btnDaily.setOnClickListener { updateRecurrenceTypeUI(TransactionSelectRecurrenceViewModel.RecurrenceType.DAILY) }
        binding.btnWeekly.setOnClickListener { updateRecurrenceTypeUI(TransactionSelectRecurrenceViewModel.RecurrenceType.WEEKLY) }
        binding.btnMonthly.setOnClickListener { updateRecurrenceTypeUI(TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY) }
        binding.btnYearly.setOnClickListener { updateRecurrenceTypeUI(TransactionSelectRecurrenceViewModel.RecurrenceType.YEARLY) }

        // Interval sliders
        // Map each slider to its corresponding TextView
        val intervalMap = mapOf(
            binding.dailyIntervalSlider to binding.dailyIntervalValue,
            binding.weeklyIntervalSlider to binding.weeklyIntervalValue,
            binding.monthlyIntervalSlider to binding.monthlyIntervalValue,
            binding.yearlyIntervalSlider to binding.yearlyIntervalValue
        )
        // Attach listeners
        intervalMap.forEach { (slider, textView) ->
            slider.addOnChangeListener { _, value, _ ->
                textView.text = "${value.toInt()}"
            }
        }

        // Chips for weekly recurrence listeners
        listOf(
            binding.chipMon,
            binding.chipTue,
            binding.chipWed,
            binding.chipThu,
            binding.chipFri,
            binding.chipSat,
            binding.chipSun
        ).forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                val colorAttr = if (isChecked) R.attr.bb_buttonSelected else R.attr.bb_surfaceAlt
                val color = requireContext().getThemeColor(colorAttr)
                chip.chipBackgroundColor = ColorStateList.valueOf(color)
            }
        }

        // End type radio group
        binding.endDateGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioEndAfter.id -> {
                    binding.occurrencesLayout.isVisible = true
                    binding.endDateLayout.isVisible = false
                }
                binding.radioEndOn.id -> {
                    binding.occurrencesLayout.isVisible = false
                    binding.endDateLayout.isVisible = true
                }
                else -> {
                    binding.occurrencesLayout.isVisible = false
                    binding.endDateLayout.isVisible = false
                }
            }
        }

        // Date picker
        binding.endDateInput.setOnClickListener {
            showDatePicker()
        }

        // Day of week radio for monthly recurrence
        binding.radioDayOfWeek.setOnCheckedChangeListener { _, _ -> }
    }

    private fun updateRecurrenceTypeUI(type: TransactionSelectRecurrenceViewModel.RecurrenceType) {
        Log.d("RecurrenceFragment", "Updating UI for recurrence type: $type")
        
        // Update button states first
        updateButtonStates(when (type) {
            TransactionSelectRecurrenceViewModel.RecurrenceType.ONCE_OFF -> binding.btnOnceOff
            TransactionSelectRecurrenceViewModel.RecurrenceType.DAILY -> binding.btnDaily
            TransactionSelectRecurrenceViewModel.RecurrenceType.WEEKLY -> binding.btnWeekly
            TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY -> binding.btnMonthly
            TransactionSelectRecurrenceViewModel.RecurrenceType.YEARLY -> binding.btnYearly
        })
        
        // Then update visibility
        binding.dailyOptions.isVisible = type == TransactionSelectRecurrenceViewModel.RecurrenceType.DAILY
        binding.weeklyOptions.isVisible = type == TransactionSelectRecurrenceViewModel.RecurrenceType.WEEKLY
        binding.monthlyOptions.isVisible = type == TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY
        binding.yearlyOptions.isVisible = type == TransactionSelectRecurrenceViewModel.RecurrenceType.YEARLY
        binding.recurrenceDetailsCard.isVisible = type != TransactionSelectRecurrenceViewModel.RecurrenceType.ONCE_OFF
        binding.endDateCard.isVisible = type != TransactionSelectRecurrenceViewModel.RecurrenceType.ONCE_OFF
    }

    private fun updateButtonStates(selectedButton: MaterialButton) {
        val selectedColor = requireContext().getThemeColor(R.attr.bb_buttonSelected)
        val defaultColor = requireContext().getThemeColor(R.attr.bb_surface)

        listOf(
            binding.btnOnceOff,
            binding.btnDaily,
            binding.btnWeekly,
            binding.btnMonthly,
            binding.btnYearly
        ).forEach { button ->
            val isSelected = button == selectedButton
            button.background.setTint(if (isSelected) selectedColor else defaultColor)
            button.isChecked = isSelected
        }
    }

    private fun showDatePicker() {
        val currentDate = calendar.time
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                // Ensure the time is set to the end of the day
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                binding.endDateInput.setText(dateFormatter.format(calendar.time))
                // Update the ViewModel with the new date
                recurrenceViewModel.setEndDate(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Set minimum date to today
            datePicker.minDate = currentDate.time
        }.show()
    }

    private fun getSelectedWeekDays(): List<String> {
        return listOf(
            binding.chipMon,
            binding.chipTue,
            binding.chipWed,
            binding.chipThu,
            binding.chipFri,
            binding.chipSat,
            binding.chipSun
        ).filter { it.isChecked }
            .map { it.text.toString() }
    }

    private fun observeViewModel() {
        recurrenceViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TransactionSelectRecurrenceViewModel.UiState.Loading -> {
                    binding.btnSave.isEnabled = false
                }
                is TransactionSelectRecurrenceViewModel.UiState.Success -> {
                    binding.btnSave.isEnabled = true
                }
                is TransactionSelectRecurrenceViewModel.UiState.Error -> {
                    binding.btnSave.isEnabled = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {
                    binding.btnSave.isEnabled = true
                }
            }
        }

        recurrenceViewModel.validationState.observe(viewLifecycleOwner) { validationState ->
            if (validationState.shouldShowErrors) {
                binding.occurrencesInput.error = if (!validationState.isOccurrencesValid) "Invalid occurrences" else null
                binding.endDateInput.error = if (!validationState.isEndDateValid) "Invalid end date" else null
                
                // Show week days validation error
                if (!validationState.isWeekDaysValid) {
                    Snackbar.make(binding.root, validationState.weekDaysError ?: "Please select at least one day", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        recurrenceViewModel.eventSave.observe(viewLifecycleOwner) { recurrenceData ->
            viewModel.setRecurrenceData(recurrenceData)
            findNavController().popBackStack()
        }
    }

    private fun restorePreviousState() {
        Log.d("RecurrenceFragment", "Starting state restoration")
        // Get the current recurrence data from the parent view model
        val currentRecurrenceData = viewModel.recurrenceData.value
        
        if (currentRecurrenceData != null && currentRecurrenceData != RecurrenceData.DEFAULT) {
            Log.d("RecurrenceFragment", "Found existing recurrence data: $currentRecurrenceData")
            // Save the state in the recurrence view model
            recurrenceViewModel.saveState(currentRecurrenceData)
            // Restore the state
            recurrenceViewModel.restoreFromSavedState()
            
            // Get the restored type directly from the ViewModel
            val restoredType = recurrenceViewModel.recurrenceType
            Log.d("RecurrenceFragment", "Using restored type: $restoredType")
            
            // Update UI based on restored state
            updateRecurrenceTypeUI(restoredType)
            
            // Restore interval values
            binding.dailyIntervalSlider.value = recurrenceViewModel.interval.toFloat()
            binding.weeklyIntervalSlider.value = recurrenceViewModel.interval.toFloat()
            binding.monthlyIntervalSlider.value = recurrenceViewModel.interval.toFloat()
            binding.yearlyIntervalSlider.value = recurrenceViewModel.interval.toFloat()
            
            // Update interval text views
            binding.dailyIntervalValue.text = recurrenceViewModel.interval.toString()
            binding.weeklyIntervalValue.text = recurrenceViewModel.interval.toString()
            binding.monthlyIntervalValue.text = recurrenceViewModel.interval.toString()
            binding.yearlyIntervalValue.text = recurrenceViewModel.interval.toString()
            
            // Restore week days
            recurrenceViewModel.weekDays.forEach { day ->
                when (day) {
                    "Mon" -> binding.chipMon.isChecked = true
                    "Tue" -> binding.chipTue.isChecked = true
                    "Wed" -> binding.chipWed.isChecked = true
                    "Thu" -> binding.chipThu.isChecked = true
                    "Fri" -> binding.chipFri.isChecked = true
                    "Sat" -> binding.chipSat.isChecked = true
                    "Sun" -> binding.chipSun.isChecked = true
                }
            }
            
            // Restore end type
            when (recurrenceViewModel.endType) {
                TransactionSelectRecurrenceViewModel.EndType.NEVER -> {
                    binding.radioNoEnd.isChecked = true
                    binding.occurrencesLayout.isVisible = false
                    binding.endDateLayout.isVisible = false
                }
                TransactionSelectRecurrenceViewModel.EndType.AFTER -> {
                    binding.radioEndAfter.isChecked = true
                    binding.occurrencesLayout.isVisible = true
                    binding.endDateLayout.isVisible = false
                    recurrenceViewModel.occurrences?.let { 
                        binding.occurrencesInput.setText(it.toString())
                    }
                }
                TransactionSelectRecurrenceViewModel.EndType.ON -> {
                    binding.radioEndOn.isChecked = true
                    binding.occurrencesLayout.isVisible = false
                    binding.endDateLayout.isVisible = true
                    recurrenceViewModel.endDate?.let { date ->
                        calendar.time = date
                        binding.endDateInput.setText(dateFormatter.format(date))
                    }
                }
            }
            
            // Restore day of week for monthly recurrence
            if (recurrenceViewModel.recurrenceType == TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY) {
                binding.radioDayOfWeek.isChecked = recurrenceViewModel.isDayOfWeek
            }
            
            Log.d("RecurrenceFragment", "State restoration completed")
        } else {
            Log.d("RecurrenceFragment", "No existing recurrence data found")
        }
    }

    private fun saveRecurrence() {
        // Get recurrence type
        val recurrenceType = when {
            binding.btnOnceOff.isChecked -> TransactionSelectRecurrenceViewModel.RecurrenceType.ONCE_OFF
            binding.btnDaily.isChecked -> TransactionSelectRecurrenceViewModel.RecurrenceType.DAILY
            binding.btnWeekly.isChecked -> TransactionSelectRecurrenceViewModel.RecurrenceType.WEEKLY
            binding.btnMonthly.isChecked -> TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY
            binding.btnYearly.isChecked -> TransactionSelectRecurrenceViewModel.RecurrenceType.YEARLY
            else -> TransactionSelectRecurrenceViewModel.RecurrenceType.ONCE_OFF
        }

        // Get interval based on recurrence type
        val interval = when (recurrenceType) {
            TransactionSelectRecurrenceViewModel.RecurrenceType.DAILY ->
                binding.dailyIntervalSlider.value.toInt()
            TransactionSelectRecurrenceViewModel.RecurrenceType.WEEKLY -> 
                binding.weeklyIntervalSlider.value.toInt()
            TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY -> 
                binding.monthlyIntervalSlider.value.toInt()
            TransactionSelectRecurrenceViewModel.RecurrenceType.YEARLY ->
                binding.yearlyIntervalSlider.value.toInt()
            else -> 1
        }

        // Get end type and related data
        val endType = when {
            binding.radioNoEnd.isChecked -> TransactionSelectRecurrenceViewModel.EndType.NEVER
            binding.radioEndAfter.isChecked -> TransactionSelectRecurrenceViewModel.EndType.AFTER
            binding.radioEndOn.isChecked -> TransactionSelectRecurrenceViewModel.EndType.ON
            else -> TransactionSelectRecurrenceViewModel.EndType.NEVER
        }

        val occurrences = if (endType == TransactionSelectRecurrenceViewModel.EndType.AFTER) {
            binding.occurrencesInput.text.toString().toIntOrNull()
        } else null

        val endDate = if (endType == TransactionSelectRecurrenceViewModel.EndType.ON) {
            calendar.time
        } else null

        // Get week days for weekly recurrence
        val weekDays = if (recurrenceType == TransactionSelectRecurrenceViewModel.RecurrenceType.WEEKLY) {
            getSelectedWeekDays()
        } else emptyList()

        // Get isDayOfWeek for monthly recurrence
        val isDayOfWeek = if (recurrenceType == TransactionSelectRecurrenceViewModel.RecurrenceType.MONTHLY) {
            binding.radioDayOfWeek.isChecked
        } else false

        // Update ViewModel with all collected data
        recurrenceViewModel.setRecurrenceType(recurrenceType)
        recurrenceViewModel.setInterval(interval)
        recurrenceViewModel.setEndType(endType)
        recurrenceViewModel.setOccurrences(occurrences)
        recurrenceViewModel.setEndDate(endDate)
        recurrenceViewModel.setWeekDays(weekDays)
        recurrenceViewModel.setIsDayOfWeek(isDayOfWeek)

        // Save the recurrence data
        recurrenceViewModel.saveRecurrenceData()

        // After saving, store the state
        recurrenceViewModel.eventSave.value?.let { recurrenceData ->
            recurrenceViewModel.saveState(recurrenceData)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
