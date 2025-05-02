package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetLabelUseCase
import com.synaptix.budgetbuddy.data.entity.LabelEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSelectLabelViewModel @Inject constructor(
    private val getLabelUseCase: GetLabelUseCase // Inject dependencies here
) : ViewModel() {

    private val _labels = MutableStateFlow<List<LabelEntity>>(emptyList())
    val labels: StateFlow<List<LabelEntity>> get() = _labels

    // Fetch labels for a specific user
    fun loadLabelsForUser(userId: Int) {
        viewModelScope.launch {
            _labels.value = getLabelUseCase.execute(userId)
        }
    }
}