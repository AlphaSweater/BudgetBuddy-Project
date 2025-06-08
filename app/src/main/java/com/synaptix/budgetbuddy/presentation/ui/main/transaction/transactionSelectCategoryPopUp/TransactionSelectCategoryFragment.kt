package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.graphics.Rect
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import androidx.navigation.navGraphViewModels

@AndroidEntryPoint
class TransactionSelectCategoryFragment : Fragment() {

    private var _binding: FragmentTransactionSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: TransactionAddViewModel by navGraphViewModels(R.id.ind_transaction_navigation_graph) {defaultViewModelProviderFactory}
    private val categoryViewModel: TransactionSelectCategoryViewModel by viewModels()

    private val expenseAdapter by lazy {
        TransactionSelectCategoryAdapter { category ->
            sharedViewModel.setCategory(category)
            findNavController().popBackStack()
        }
    }

    private val incomeAdapter by lazy {
        TransactionSelectCategoryAdapter { category ->
            sharedViewModel.setCategory(category)
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupSearch()
        observeViewModel()
        categoryViewModel.loadCategories()
    }

    private fun setupViews() {
        with(binding) {
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnAddCategory.setOnClickListener { showAddCategory() }
            btnAddCategoryEmpty.setOnClickListener { showAddCategory() }

            btnExpenseToggle.setOnClickListener { showExpenseCategories() }
            btnIncomeToggle.setOnClickListener { showIncomeCategories() }

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

        binding.recyclerViewIncomeCategory.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = incomeAdapter
            addItemDecoration(gridSpacing)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            categoryViewModel.filterCategories(text?.toString() ?: "")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    categoryViewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }

                // Collect filtered categories
                launch {
                    categoryViewModel.filteredCategories.collect { categories ->
                        val (expenseCategories, incomeCategories) = categories.partition { it.type == "expense" }
                        expenseAdapter.submitList(expenseCategories)
                        incomeAdapter.submitList(incomeCategories)
                        updateEmptyState()
                    }
                }
            }
        }
    }

    private fun handleUiState(state: TransactionSelectCategoryViewModel.UiState) {
        when (state) {
            is TransactionSelectCategoryViewModel.UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
            }
            is TransactionSelectCategoryViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
            is TransactionSelectCategoryViewModel.UiState.Error -> {
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

    private fun showExpenseCategories() {
        with(binding) {
            recyclerViewExpenseCategory.visibility = View.VISIBLE
            recyclerViewIncomeCategory.visibility = View.GONE
            btnExpenseToggle.setBackgroundResource(R.drawable.toggle_selected)
            btnIncomeToggle.background = null
            updateEmptyState()
        }
    }

    private fun showIncomeCategories() {
        with(binding) {
            recyclerViewExpenseCategory.visibility = View.GONE
            recyclerViewIncomeCategory.visibility = View.VISIBLE
            btnIncomeToggle.setBackgroundResource(R.drawable.toggle_selected)
            btnExpenseToggle.background = null
            updateEmptyState()
        }
    }

    private fun updateEmptyState() {
        with(binding) {
            val isExpenseVisible = recyclerViewExpenseCategory.isVisible
            val currentAdapter = if (isExpenseVisible) expenseAdapter else incomeAdapter
            emptyState.visibility = if (currentAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun showAddCategory() {
        findNavController().navigate(R.id.navigation_category_add_new)
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