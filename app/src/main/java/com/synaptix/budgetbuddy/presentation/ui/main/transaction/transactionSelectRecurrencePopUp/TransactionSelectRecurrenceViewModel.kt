package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectRecurrencePopUp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import java.util.Calendar
import java.util.Date

class TransactionSelectRecurrenceViewModel : ViewModel() {
    sealed class UiState {
        object Initial : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    data class ValidationState(
        val isRecurrenceTypeValid: Boolean = true,
        val isEndTypeValid: Boolean = true,
        val isIntervalValid: Boolean = true,
        val isWeekDaysValid: Boolean = true,
        val isEndDateValid: Boolean = true,
        val isOccurrencesValid: Boolean = true,
        val shouldShowErrors: Boolean = false,
        val weekDaysError: String? = null
    )

    private val _uiState = MutableLiveData<UiState>(UiState.Initial)
    val uiState: LiveData<UiState> = _uiState

    private val _validationState = MutableLiveData(ValidationState())
    val validationState: LiveData<ValidationState> = _validationState

    private val _eventSave = MutableLiveData<RecurrenceData>()
    val eventSave: LiveData<RecurrenceData> = _eventSave

    // Form fields
    // Private properties and getters for the recurrence data
    private var _recurrenceType: RecurrenceType = RecurrenceType.ONCE_OFF
    val recurrenceType: RecurrenceType
        get() = _recurrenceType

    private var _endType: EndType = EndType.NEVER
    val endType: EndType
        get() = _endType

    private var _endDate: Date? = null
    val endDate: Date?
        get() = _endDate

    private var _occurrences: Int? = null
    val occurrences: Int?
        get() = _occurrences

    private var _interval: Int = 1
    val interval: Int
        get() = _interval

    private var _weekDays: List<String> = emptyList()
    val weekDays: List<String>
        get() = _weekDays

    private var _isDayOfWeek: Boolean = false
    val isDayOfWeek: Boolean
        get() = _isDayOfWeek


    // Add state management
    private var savedState: RecurrenceData? = null

    fun setRecurrenceType(type: RecurrenceType) {
        _recurrenceType = type
    }

    fun setEndType(type: EndType) {
        _endType = type
    }

    fun setEndDate(date: Date?) {
        _endDate = date
    }

    fun setOccurrences(count: Int?) {
        _occurrences = count
    }

    fun setInterval(value: Int) {
        _interval = value
    }

    fun setWeekDays(days: List<String>) {
        _weekDays = days
    }

    fun setIsDayOfWeek(value: Boolean) {
        _isDayOfWeek = value
    }

    fun saveRecurrenceData() {
        if (!validateForm()) {
            _validationState.value = _validationState.value?.copy(shouldShowErrors = true)
            return
        }

        _uiState.value = UiState.Loading
        try {
            val recurrenceData = when (_recurrenceType) {
                RecurrenceType.ONCE_OFF -> RecurrenceData.DEFAULT
                RecurrenceType.DAILY -> createRecurrenceData(
                    "Daily",
                    _interval,
                    _endType,
                    _occurrences,
                    _endDate
                )
                RecurrenceType.WEEKLY -> createRecurrenceData(
                    "Weekly",
                    _interval,
                    _endType,
                    _occurrences,
                    _endDate,
                    weekDays = _weekDays
                )
                RecurrenceType.MONTHLY -> createRecurrenceData(
                    "Monthly",
                    _interval,
                    _endType,
                    _occurrences,
                    _endDate,
                    isDayOfWeek = _isDayOfWeek
                )
                RecurrenceType.YEARLY -> createRecurrenceData(
                    "Yearly",
                    _interval,
                    _endType,
                    _occurrences,
                    _endDate
                )
            }
            _eventSave.value = recurrenceData
            _uiState.value = UiState.Success
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Failed to save recurrence data")
        }
    }

    private fun validateForm(): Boolean {
        val isIntervalValid = _interval > 0
        val isWeekDaysValid = _recurrenceType != RecurrenceType.WEEKLY || _weekDays.isNotEmpty()
        val isEndDateValid = _endType != EndType.ON || (_endDate != null && _endDate!!.after(Date()))
        val isOccurrencesValid = _endType != EndType.AFTER || (_occurrences ?: 0) > 0

        _validationState.value = ValidationState(
            isIntervalValid = isIntervalValid,
            isWeekDaysValid = isWeekDaysValid,
            isEndDateValid = isEndDateValid,
            isOccurrencesValid = isOccurrencesValid,
            weekDaysError = if (_recurrenceType == RecurrenceType.WEEKLY && _weekDays.isEmpty()) 
                "Please select at least one day" else null
        )

        return isIntervalValid &&
               isWeekDaysValid && isEndDateValid && isOccurrencesValid
    }

    private fun createRecurrenceData(
        type: String,
        interval: Int,
        endType: EndType,
        occurrences: Int?,
        endDate: Date?,
        weekDays: List<String> = emptyList(),
        isDayOfWeek: Boolean = false
    ) = RecurrenceData(
        type = type,
        interval = interval,
        weekDays = weekDays,
        isDayOfWeek = isDayOfWeek,
        endType = endType.toString(),
        endValue = when (endType) {
            EndType.AFTER -> occurrences?.toString()
            EndType.ON -> endDate?.let { formatDate(it) }
            EndType.NEVER -> null
        }
    )

    private fun formatDate(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        // Format as MM/dd/yyyy for consistency
        return String.format("%02d/%02d/%04d",
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.YEAR)
        )
    }

    fun saveState(recurrenceData: RecurrenceData) {
        savedState = recurrenceData
        Log.d("RecurrenceVM", "State saved: $recurrenceData")
    }

    fun restoreFromSavedState() {
        Log.d("RecurrenceVM", "Attempting to restore state: $savedState")
        savedState?.let { state ->
            // Restore recurrence type
            _recurrenceType = when (state.type) {
                "Once Off" -> RecurrenceType.ONCE_OFF
                "Daily" -> RecurrenceType.DAILY
                "Weekly" -> RecurrenceType.WEEKLY
                "Monthly" -> RecurrenceType.MONTHLY
                "Yearly" -> RecurrenceType.YEARLY
                else -> RecurrenceType.ONCE_OFF
            }
            Log.d("RecurrenceVM", "Restored type: ${_recurrenceType}")

            // Restore interval
            _interval = state.interval
            Log.d("RecurrenceVM", "Restored interval: ${_interval}")

            // Restore week days
            _weekDays = state.weekDays
            Log.d("RecurrenceVM", "Restored weekDays: ${_weekDays}")

            // Restore isDayOfWeek
            _isDayOfWeek = state.isDayOfWeek
            Log.d("RecurrenceVM", "Restored isDayOfWeek: ${_isDayOfWeek}")

            // Restore end type and related data
            _endType = when (state.endType) {
                "NEVER" -> EndType.NEVER
                "AFTER" -> EndType.AFTER
                "ON" -> EndType.ON
                else -> EndType.NEVER
            }
            Log.d("RecurrenceVM", "Restored endType: ${_endType}")

            // Restore occurrences or end date
            when (_endType) {
                EndType.AFTER -> {
                    _occurrences = state.endValue?.toIntOrNull()
                    Log.d("RecurrenceVM", "Restored occurrences: ${_occurrences}")
                }
                EndType.ON -> {
                    state.endValue?.let { dateStr ->
                        try {
                            val parts = dateStr.split("/")
                            if (parts.size == 3) {
                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.MONTH, parts[0].toInt() - 1)
                                calendar.set(Calendar.DAY_OF_MONTH, parts[1].toInt())
                                calendar.set(Calendar.YEAR, parts[2].toInt())
                                _endDate = calendar.time
                                Log.d("RecurrenceVM", "Restored endDate: ${_endDate}")
                            }
                        } catch (e: Exception) {
                            Log.e("RecurrenceVM", "Error parsing date: ${e.message}")
                        }
                    }
                }
                else -> {
                    _occurrences = null
                    _endDate = null
                }
            }
        }
    }

    enum class RecurrenceType {
        ONCE_OFF, DAILY, WEEKLY, MONTHLY, YEARLY
    }

    enum class EndType {
        NEVER, AFTER, ON
    }
}