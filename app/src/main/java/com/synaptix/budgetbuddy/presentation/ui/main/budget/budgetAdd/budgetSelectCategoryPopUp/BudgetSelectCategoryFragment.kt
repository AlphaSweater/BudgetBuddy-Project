//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectCategoryPopUp

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentBudgetSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.BudgetAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetSelectCategoryFragment : Fragment() {
    private var _binding: FragmentBudgetSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val addBudgetViewModel: BudgetAddViewModel by activityViewModels()
    private val selectCategoryViewModel: BudgetSelectCategoryViewModel by viewModels()

    private val expenseAdapter by lazy {
        BudgetSelectCategoryAdapter { selectedCategories ->
            selectCategoryViewModel.updateSelectedCategories(selectedCategories)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetSelectCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupSearch()
        observeViewModel()
        selectCategoryViewModel.loadCategories()
    }

    private fun setupViews() {
        with(binding) {
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnSave.setOnClickListener {
                val selectedCategories = expenseAdapter.getSelectedCategories()
                if (selectedCategories.isNotEmpty()) {
                    addBudgetViewModel.setSelectedCategories(selectedCategories)
                    findNavController().popBackStack()
                } else {
                    showError("Please select at least one category")
                }
            }

            setupRecyclerViews()
        }
    }

    private fun setupRecyclerViews() {
        val gridSpacing = GridSpacingItemDecoration(2, 8, true)

        binding.recyclerViewExpenseCategory.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = expenseAdapter
            addItemDecoration(gridSpacing)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            selectCategoryViewModel.filterCategories(text?.toString() ?: "")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    selectCategoryViewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }

                // Collect filtered categories
                launch {
                    selectCategoryViewModel.filteredCategories.collect { categories ->
                        val expenseCategories = categories.filter { it.type == "expense" }
                        expenseAdapter.submitList(expenseCategories, selectCategoryViewModel.getSelectedCategories())
                        updateEmptyState()
                    }
                }
            }
        }
    }

    private fun handleUiState(state: BudgetSelectCategoryViewModel.UiState) {
        when (state) {
            is BudgetSelectCategoryViewModel.UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
            }
            is BudgetSelectCategoryViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
            is BudgetSelectCategoryViewModel.UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
                showError(state.message)
            }
            else -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun updateEmptyState() {
        with(binding) {
            val isExpenseVisible = recyclerViewExpenseCategory.isVisible
            val currentAdapter = if (isExpenseVisible) expenseAdapter else null
            emptyState.visibility = if (currentAdapter?.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }
}

