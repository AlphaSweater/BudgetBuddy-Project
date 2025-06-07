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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSelectLabelViewModel @Inject constructor(
    private val getLabelUseCase: GetLabelUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _labels = MutableStateFlow<List<Label>>(emptyList())
    val labels: StateFlow<List<Label>> = _labels

    private val _filteredLabels = MutableStateFlow<List<Label>>(emptyList())
    val filteredLabels: StateFlow<List<Label>> = _filteredLabels

    private var searchQuery: String = ""

    fun loadLabelsForUser() {
        viewModelScope.launch {
            val userId = getUserIdUseCase.execute()
            when (val result = getLabelUseCase.execute(userId)) {
                is GetLabelUseCase.GetLabelsResult.Success -> {
                    _labels.value = result.labels
                    filterLabels()
                }
                is GetLabelUseCase.GetLabelsResult.Error -> {
                    // Handle error case if needed
                }
            }
        }
    }

    fun filterLabels(query: String = searchQuery) {
        searchQuery = query
        _filteredLabels.value = if (query.isEmpty()) {
            _labels.value
        } else {
            _labels.value.filter { label ->
                label.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun shouldShowCreateNew(): Boolean {
        return searchQuery.isNotEmpty() && _filteredLabels.value.none { 
            it.name.equals(searchQuery, ignoreCase = true) 
        }
    }

    fun updateSelectedLabels(selectedLabels: List<Label>) {
        _labels.update { currentLabels ->
            currentLabels.map { label ->
                label.copy(isSelected = selectedLabels.contains(label))
            }
        }
        filterLabels()
    }
}