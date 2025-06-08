package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.User
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.core.usecase.main.label.AddLabelUseCase
import com.synaptix.budgetbuddy.core.usecase.main.label.GetLabelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSelectLabelViewModel @Inject constructor(
    private val getLabelsUseCase: GetLabelsUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val addLabelUseCase: AddLabelUseCase
) : ViewModel() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // State Properties
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _labels = MutableStateFlow<List<Label>>(emptyList())
    val labels: StateFlow<List<Label>> = _labels

    private val _filteredLabels = MutableStateFlow<List<Label>>(emptyList())
    val filteredLabels: StateFlow<List<Label>> = _filteredLabels

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var searchQuery: String = ""

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Data Loading Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun loadLabelsForUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = getUserIdUseCase.execute()
                Log.d("TransactionSelectLabelViewModel", "Loading labels for user: $userId")
                
                if (userId.isEmpty()) {
                    Log.e("TransactionSelectLabelViewModel", "User ID is empty")
                    _error.value = "User ID is empty"
                    return@launch
                }

                getLabelsUseCase.execute(userId)
                    .catch { e ->
                        Log.e("TransactionSelectLabelViewModel", "Error loading labels: ${e.message}")
                        _error.value = "Failed to load labels: ${e.message}"
                    }
                    .collect { result ->
                        when (result) {
                            is GetLabelsUseCase.GetLabelsResult.Success -> {
                                Log.d("TransactionSelectLabelViewModel", "Received ${result.labels.size} labels")
                                _labels.value = result.labels
                                filterLabels()
                                _isLoading.value = false
                            }
                            is GetLabelsUseCase.GetLabelsResult.Error -> {
                                Log.e("TransactionSelectLabelViewModel", "Error result: ${result.message}")
                                _error.value = result.message
                                _isLoading.value = false
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TransactionSelectLabelViewModel", "Exception loading labels: ${e.message}")
                _error.value = "Failed to load labels: ${e.message}"
                _isLoading.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Label Management Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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

    fun updateSelectedLabels(selectedLabels: List<Label>) {
        _labels.update { currentLabels ->
            currentLabels.map { label ->
                label.copy(isSelected = selectedLabels.contains(label))
            }
        }
        filterLabels()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Label Creation Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    fun createNewLabel(name: String) {
        viewModelScope.launch {
            try {
                val userId = getUserIdUseCase.execute()
                if (userId.isEmpty()) {
                    _error.value = "User ID is empty"
                    return@launch
                }

                val user = User(userId, "", "", "")
                val newLabel = Label.new(
                    user = user,
                    name = name
                )

                addLabelUseCase.execute(newLabel).collect { result ->
                    when (result) {
                        is AddLabelUseCase.AddLabelResult.Success -> {
                            Log.d("TransactionSelectLabelViewModel", "Label created successfully: ${result.labelId}")
                            // Labels will be automatically updated through the Flow
                        }
                        is AddLabelUseCase.AddLabelResult.Error -> {
                            _error.value = result.message
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TransactionSelectLabelViewModel", "Exception creating label: ${e.message}")
                _error.value = "Failed to create label: ${e.message}"
            }
        }
    }

    fun validateLabelName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length <= 50 && // Adjust max length as needed
               !_labels.value.any { it.name.equals(name, ignoreCase = true) }
    }

    fun clearError() {
        _error.value = null
    }
}