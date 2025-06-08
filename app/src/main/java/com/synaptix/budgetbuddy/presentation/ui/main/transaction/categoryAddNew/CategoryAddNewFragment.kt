package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentCategoryAddNewBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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

        applyScreenMode(viewModel.screenMode.value)
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

        // Navigation and actions
        binding.btnGoBack.setOnClickListener {
            when (viewModel.screenMode.value) {
                CategoryAddNewViewModel.ScreenMode.EDIT -> {
                    if (viewModel.hasUnsavedChanges.value) {
                        showDiscardChangesDialog()
                    } else {
                        findNavController().popBackStack()
                    }
                }
                CategoryAddNewViewModel.ScreenMode.CREATE -> {
                    findNavController().popBackStack()
                }
            }
        }

        binding.btnClear.setOnClickListener {
            viewModel.reset()
        }

        binding.btnEdit.setOnClickListener {
            viewModel.setScreenMode(CategoryAddNewViewModel.ScreenMode.EDIT)
            applyScreenMode(CategoryAddNewViewModel.ScreenMode.EDIT)
        }

        binding.btnCreate.setOnClickListener {
            viewModel.createCategory()
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Screen Mode Handling
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun applyScreenMode(mode: CategoryAddNewViewModel.ScreenMode) {
        when (mode) {
            CategoryAddNewViewModel.ScreenMode.EDIT -> applyEditMode()
            CategoryAddNewViewModel.ScreenMode.CREATE -> applyCreateMode()
        }
    }

    private fun applyEditMode() {
        binding.apply {
            btnEdit.visibility = View.GONE
            btnClear.visibility = View.VISIBLE
            btnCreate.apply {
                text = "Update"
                visibility = View.VISIBLE
            }
            toolbarTitle.text = "Edit Category"
            
            // Update content margin when bottom container is visible
            contentScrollView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_margin)
            }
            
            enableAllInteractiveElements()
        }
    }

    private fun applyCreateMode() {
        binding.apply {
            btnEdit.visibility = View.GONE
            btnClear.visibility = View.VISIBLE
            btnCreate.apply {
                text = "Create"
                visibility = View.VISIBLE
            }
            toolbarTitle.text = "Add New Category"
            
            // Update content margin when bottom container is visible
            contentScrollView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_margin)
            }
            
            enableAllInteractiveElements()
        }
    }

    private fun disableAllInteractiveElements() {
        binding.apply {
            categoryNameInput.isEnabled = false
            btnExpenseToggle.isEnabled = false
            btnIncomeToggle.isEnabled = false
            recyclerViewColors.isEnabled = false
            recyclerViewIcons.isEnabled = false
        }
    }

    private fun enableAllInteractiveElements() {
        binding.apply {
            categoryNameInput.isEnabled = true
            btnExpenseToggle.isEnabled = true
            btnIncomeToggle.isEnabled = true
            recyclerViewColors.isEnabled = true
            recyclerViewIcons.isEnabled = true
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State Handlers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun handleLoadingUiState(state: CategoryAddNewViewModel.LoadingUiState) {
        when (state) {
            is CategoryAddNewViewModel.LoadingUiState.Loading -> {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successCheckmark.visibility = View.GONE
                binding.loadingText.text = "Loading..."
            }
            is CategoryAddNewViewModel.LoadingUiState.Loaded -> {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                populateInitialFormValues()
            }
            is CategoryAddNewViewModel.LoadingUiState.Error -> {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                showError(state.message)
            }
            else -> {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleSavingUiState(state: CategoryAddNewViewModel.SavingUiState) {
        when (state) {
            is CategoryAddNewViewModel.SavingUiState.Saving -> {
                binding.btnCreate.isEnabled = false
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successCheckmark.visibility = View.GONE
                binding.loadingText.text = when (viewModel.screenMode.value) {
                    CategoryAddNewViewModel.ScreenMode.EDIT -> "Updating category..."
                    else -> "Creating category..."
                }
            }
            is CategoryAddNewViewModel.SavingUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.successCheckmark.visibility = View.VISIBLE
                binding.loadingText.text = when (viewModel.screenMode.value) {
                    CategoryAddNewViewModel.ScreenMode.EDIT -> "Category updated successfully!"
                    else -> "Category created successfully!"
                }
                
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    binding.loadingOverlay.visibility = View.GONE
                    viewModel.reset()
                    findNavController().popBackStack()
                }
            }
            is CategoryAddNewViewModel.SavingUiState.Error -> {
                binding.btnCreate.isEnabled = true
                binding.loadingOverlay.visibility = View.GONE
                showError(state.message)
            }
            else -> {
                binding.btnCreate.isEnabled = true
                binding.loadingOverlay.visibility = View.GONE
            }
        }
    }

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

    private fun showDiscardChangesDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Discard") { _, _ ->
                // Temporarily remove text watchers
                binding.categoryNameInput.removeTextChangedListener(binding.categoryNameInput.tag as? TextWatcher)

                // Revert changes in ViewModel
                viewModel.revertChanges()

                // Update text fields with reverted values
                binding.categoryNameInput.setText(viewModel.categoryName.value)

                // Navigate back
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            // Set button colors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.expense_red)
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.profit_green)
            )

            val background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog_rounded)
            dialog.window?.setBackgroundDrawable(background)
        }

        dialog.show()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Form Handling
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun populateInitialFormValues() {
        val name = viewModel.categoryName.value

        // Set values manually
        binding.categoryNameInput.setText(name)

        // Update appearance
        updateCategoryAppearance()

        // Set selected color and icon in adapters if in EDIT mode
        if (viewModel.screenMode.value == CategoryAddNewViewModel.ScreenMode.EDIT) {
            viewModel.selectedColor.value?.let { color ->
                colorAdapter.setSelectedItem(color)
            }
            viewModel.selectedIcon.value?.let { icon ->
                iconAdapter.setSelectedItem(icon)
            }
        }
    }

    private fun updateCategoryAppearance() {
        binding.apply {
            val selectedColor = viewModel.selectedColor.value
            val selectedIcon = viewModel.selectedIcon.value

            if (selectedColor == null || selectedIcon == null) {
                previewIcon.setColorFilter(requireContext().getThemeColor(R.attr.bb_accent))
            } else {
                previewIcon.setColorFilter(requireContext().getColor(selectedColor.colorResourceId))
                previewIcon.setImageResource(selectedIcon.iconResourceId)
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // ViewModel Observers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.screenMode.collect { mode ->
                    applyScreenMode(mode)
                } }
                launch { viewModel.loadingUiState.collect { state ->
                    handleLoadingUiState(state)
                } }
                launch { viewModel.savingUiState.collect { state ->
                    handleSavingUiState(state)
                } }
                launch { viewModel.validationState.collect { state ->
                    handleValidationState(state)
                } }
                launch { viewModel.categoryName.collect { name ->
                    if (!isUpdatingFromUser && binding.categoryNameInput.text.toString() != name) {
                        binding.categoryNameInput.setText(name)
                    }
                } }
                launch { viewModel.categoryType.collect { type ->
                    binding.btnExpenseToggle.isSelected = type == "Expense"
                    binding.btnIncomeToggle.isSelected = type == "Income"
                } }
                launch { viewModel.selectedColor.collect { color ->
                    color?.let {
                        binding.previewIcon.setColorFilter(requireContext().getColor(it.colorResourceId))
                    }
                } }
                launch { viewModel.selectedIcon.collect { icon ->
                    icon?.let {
                        binding.previewIcon.setImageResource(it.iconResourceId)
                    }
                } }
                // Available Options
                launch { viewModel.colors.collect { colors ->
                    colorAdapter.submitList(colors.map { CategoryItem.ColorItem(it.colorResourceId, it.name) })
                } }
                launch { viewModel.icons.collect { icons ->
                    iconAdapter.submitList(icons.map { CategoryItem.IconItem(it.iconResourceId, it.name) })
                } }
                // Unsaved Changes
                launch { viewModel.hasUnsavedChanges.collect { hasChanges ->
                    binding.btnClear.isEnabled = hasChanges
                } }
            }
        }
    }
}
