package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentCategoryAddNewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Fragment for adding a new category.
 * Handles user input for category name, type, color, and icon selection.
 */
@AndroidEntryPoint
class CategoryAddNewFragment : Fragment() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Properties
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private var _binding: FragmentCategoryAddNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryAddNewViewModel by viewModels()
    private lateinit var colorAdapter: CategoryItemAdapter
    private lateinit var iconAdapter: CategoryItemAdapter

    private var isUpdatingFromUser = false

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Fragment Lifecycle
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryAddNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // View Setup
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setupViews() {
        setupAdapters()
        setupListeners()
    }

    /**
     * Sets up the RecyclerView adapters for colors and icons.
     * Configures horizontal scrolling for both lists.
     */
    private fun setupAdapters() {
        // Color adapter setup
        colorAdapter = CategoryItemAdapter { item ->
            if (item is CategoryItem.ColorItem) {
                viewModel.setSelectedColor(item)
            }
        }
        binding.recyclerViewColors.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }

        // Icon adapter setup
        iconAdapter = CategoryItemAdapter { item ->
            if (item is CategoryItem.IconItem) {
                viewModel.setSelectedIcon(item)
            }
        }
        binding.recyclerViewIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), 2).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = iconAdapter
        }
    }

    /**
     * Sets up click listeners for all interactive elements.
     */
    private fun setupListeners() {
        // Category name input
        binding.categoryNameInput.doAfterTextChanged { text ->
            if (!isUpdatingFromUser) {
                isUpdatingFromUser = true
                viewModel.setCategoryName(text?.toString() ?: "")
                isUpdatingFromUser = false
            }
        }

        // Category type selection
        binding.btnExpenseToggle.setOnClickListener {
            viewModel.setCategoryType("Expense")
        }

        binding.btnIncomeToggle.setOnClickListener {
            viewModel.setCategoryType("Income")
        }

        // Navigation
        binding.btnGoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Category creation
        binding.btnCreate.setOnClickListener {
            viewModel.createCategory()
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // State Handlers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    /**
     * Handles the UI state changes from the ViewModel.
     */
    private fun handleUiState(state: CategoryAddNewViewModel.UiState) {
        when (state) {
            is CategoryAddNewViewModel.UiState.Loading -> {
                binding.btnCreate.isEnabled = false
            }
            is CategoryAddNewViewModel.UiState.Success -> {
                showSuccess("Category added successfully")
                findNavController().popBackStack()
            }
            is CategoryAddNewViewModel.UiState.Error -> {
                binding.btnCreate.isEnabled = false
                showError(state.message)
            }
            else -> {
                binding.btnCreate.isEnabled = true
            }
        }
    }

    /**
     * Handles validation state changes and updates error messages.
     */
    private fun handleValidationState(state: CategoryAddNewViewModel.ValidationState) {
        with(binding) {
            textNameError.apply {
                text = state.nameError
                visibility = if (state.shouldShowErrors && state.nameError != null) View.VISIBLE else View.GONE
            }

            textTypeError.apply {
                text = state.typeError
                visibility = if (state.shouldShowErrors && state.typeError != null) View.VISIBLE else View.GONE
            }

            textColorError.apply {
                text = state.colorError
                visibility = if (state.shouldShowErrors && state.colorError != null) View.VISIBLE else View.GONE
            }

            textIconError.apply {
                text = state.iconError
                visibility = if (state.shouldShowErrors && state.iconError != null) View.VISIBLE else View.GONE
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI Helpers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // ViewModel Observers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    /**
     * Sets up observers for all ViewModel state flows.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // UI State
                launch {
                    viewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }
                
                // Validation State
                launch {
                    viewModel.validationState.collect { state ->
                        handleValidationState(state)
                    }
                }

                // Form State
                launch {
                    viewModel.categoryName.collect { name ->
                        if (!isUpdatingFromUser && binding.categoryNameInput.text.toString() != name) {
                            binding.categoryNameInput.setText(name)
                        }
                    }
                }

                launch {
                    viewModel.categoryType.collect { type ->
                        binding.btnExpenseToggle.isSelected = type == "Expense"
                        binding.btnIncomeToggle.isSelected = type == "Income"
                    }
                }

                // Selection State
                launch {
                    viewModel.selectedColor.collect { color ->
                        color?.let {
                            binding.previewIcon.setColorFilter(requireContext().getColor(it.colorResourceId))
                        }
                    }
                }

                launch {
                    viewModel.selectedIcon.collect { icon ->
                        icon?.let {
                            binding.previewIcon.setImageResource(it.iconResourceId)
                        }
                    }
                }

                // Available Options
                launch {
                    viewModel.colors.collect { colors ->
                        colorAdapter.submitList(colors.map { CategoryItem.ColorItem(it.colorResourceId, it.name) })
                    }
                }

                launch {
                    viewModel.icons.collect { icons ->
                        iconAdapter.submitList(icons.map { CategoryItem.IconItem(it.iconResourceId, it.name) })
                    }
                }
            }
        }
    }
}
