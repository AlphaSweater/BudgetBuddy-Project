package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    private val viewModel: CategoryAddNewViewModel by activityViewModels()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var iconAdapter: IconAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryAddNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAdapters() {
        colorAdapter = ColorAdapter { color ->
            viewModel.setSelectedColor(color)
        }
        
        iconAdapter = IconAdapter { icon ->
            viewModel.setSelectedIcon(icon)
        }

        binding.recyclerViewColors.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = colorAdapter
        }

        binding.recyclerViewIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = iconAdapter
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
                binding.previewIcon.setBackgroundColor(requireContext().getColor(it.colorValue))
            }
        }

        viewModel.selectedIcon.observe(viewLifecycleOwner) { icon ->
            icon?.let {
                binding.previewIcon.setImageResource(it.iconResourceId)
            }
        }

        viewModel.eventCategoryCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.statusMessage.text = "Category Created Successfully!"
                binding.statusMessage.setTextColor(requireContext().getColor(R.color.profit_green))
                binding.categoryNameInput.text?.clear()
                binding.previewIcon.setImageResource(R.drawable.ic_circle_24)
            } else {
                binding.statusMessage.text = "Please enter a name and choose an icon and color"
                binding.statusMessage.setTextColor(requireContext().getColor(R.color.expense_red))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCreate.setOnClickListener {
            viewModel.categoryName.value = binding.categoryNameInput.text.toString()
            viewModel.createCategory()
        }

        binding.btnExpenseToggle.setOnClickListener {
            viewModel.categoryType.value = "Expense"
            updateToggleState(true)
        }

        binding.btnIncomeToggle.setOnClickListener {
            viewModel.categoryType.value = "Income"
            updateToggleState(false)
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateToggleState(isExpense: Boolean) {
        binding.btnExpenseToggle.isSelected = isExpense
        binding.btnIncomeToggle.isSelected = !isExpense
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}