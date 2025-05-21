package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.label.GetLabelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSelectLabelViewModel @Inject constructor(
    private val getLabelUseCase: GetLabelUseCase, // Inject dependencies here
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _labels = MutableStateFlow<List<Label>>(emptyList())
    val labels: StateFlow<List<Label>> get() = _labels

    // Fetch labels for a specific user
    fun loadLabelsForUser() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            when (val result = getLabelUseCase.execute(userId)) {
                is GetLabelUseCase.GetLabelsResult.Success -> {
                    _labels.value = result.labels
                    Log.d("TransactionSelectLabelViewModel", "Labels loaded: ${result.labels}")
                }
                is GetLabelUseCase.GetLabelsResult.Error -> {
                    Log.e("TransactionSelectLabelViewModel", "Error loading labels: ${result.message}")
                }
            }
        }
    }
}