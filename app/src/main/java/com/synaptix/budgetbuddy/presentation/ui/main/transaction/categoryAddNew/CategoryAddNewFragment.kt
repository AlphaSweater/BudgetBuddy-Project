package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentCategoryAddNewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryAddNewFragment : Fragment() {

    private var _binding: FragmentCategoryAddNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryAddNewViewModel by viewModels()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var iconAdapter: IconAdapter

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
        setupObservers()
        setupInitialState()
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
            viewModel.categoryName.value = text?.toString()
        }

        binding.btnExpenseToggle.setOnClickListener {
            viewModel.setCategoryType("Expense")
            updateToggleState(true)
        }

        binding.btnIncomeToggle.setOnClickListener {
            viewModel.setCategoryType("Income")
            updateToggleState(false)
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCreate.setOnClickListener {
            viewModel.createCategory()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.colors.collect { colors ->
                colorAdapter.submitList(colors)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.icons.collect { icons ->
                iconAdapter.submitList(icons)
            }
        }

        viewModel.selectedColor.observe(viewLifecycleOwner) { color ->
            color?.let {
                binding.previewIcon.setColorFilter(requireContext().getColor(it.colorResourceId))
            }
        }

        viewModel.selectedIcon.observe(viewLifecycleOwner) { icon ->
            icon?.let {
                binding.previewIcon.setImageResource(it.iconResourceId)
            }
        }

        viewModel.categoryType.observe(viewLifecycleOwner) { type ->
            updateToggleState(type == "Expense")
        }

        viewModel.eventCategoryCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.statusMessage.text = getString(R.string.category_created_success)
                binding.statusMessage.setTextColor(requireContext().getColor(R.color.profit_green))
                clearForm()
            } else {
                binding.statusMessage.text = getString(R.string.category_creation_error)
                binding.statusMessage.setTextColor(requireContext().getColor(R.color.expense_red))
            }
        }
    }

    private fun setupInitialState() {
        updateToggleState(true)
    }

    private fun updateToggleState(isExpense: Boolean) {
        binding.btnExpenseToggle.isSelected = isExpense
        binding.btnIncomeToggle.isSelected = !isExpense

        // Update background tint
        binding.btnExpenseToggle.setBackgroundResource(
            if (isExpense) R.drawable.toggle_selected else android.R.color.transparent
        )
        binding.btnIncomeToggle.setBackgroundResource(
            if (!isExpense) R.drawable.toggle_selected else android.R.color.transparent
        )
    }

    private fun clearForm() {
        binding.categoryNameInput.text?.clear()
        binding.previewIcon.setImageResource(R.drawable.ic_circle_24)
        binding.previewIcon.clearColorFilter()
        setupInitialState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}