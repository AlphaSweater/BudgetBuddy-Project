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
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentCategoryAddNewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryAddNewFragment : Fragment() {

    private var _binding: FragmentCategoryAddNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryAddNewViewModel by viewModels()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var iconAdapter: IconAdapter

    private var isUpdatingFromUser = false

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
        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        colorAdapter = ColorAdapter { color ->
            viewModel.setSelectedColor(color)
        }
        binding.recyclerViewColors.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = colorAdapter
        }

        iconAdapter = IconAdapter { icon ->
            viewModel.setSelectedIcon(icon)
        }
        binding.recyclerViewIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = iconAdapter
        }
    }

    private fun setupListeners() {
        binding.categoryNameInput.doAfterTextChanged { text ->
            if (!isUpdatingFromUser) {
                isUpdatingFromUser = true
                viewModel.setCategoryName(text?.toString() ?: "")
                isUpdatingFromUser = false
            }
        }

        binding.btnExpenseToggle.setOnClickListener {
            viewModel.setCategoryType("Expense")
        }

        binding.btnIncomeToggle.setOnClickListener {
            viewModel.setCategoryType("Income")
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCreate.setOnClickListener {
            viewModel.createCategory()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    viewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }
                
                // Collect validation state
                launch {
                    viewModel.validationState.collect { state ->
                        handleValidationState(state)
                    }
                }

                // Collect form state
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

                // Collect available options
                launch {
                    viewModel.colors.collect { colors ->
                        colorAdapter.submitList(colors)
                    }
                }

                launch {
                    viewModel.icons.collect { icons ->
                        iconAdapter.submitList(icons)
                    }
                }
            }
        }
    }

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

    private fun handleValidationState(state: CategoryAddNewViewModel.ValidationState) {
        with(binding) {
            // Show/hide error messages for each field
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
