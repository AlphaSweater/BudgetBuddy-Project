package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.CategoryIn
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionSelectCategoryFragment : Fragment() {

    @Inject
    lateinit var getUserIdUseCase: GetUserIdUseCase
    private var _binding: FragmentTransactionSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()
    private val categoryViewModel: TransactionSelectCategoryViewModel by viewModels()

    private var expenseAdapter: TransactionSelectCategoryAdapter? = null
    private var incomeAdapter: TransactionSelectCategoryAdapter? = null

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
        loadCategories()
    }

    private fun setupViews() {
        with(binding) {
            // Setup click listeners
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnAddCategory.setOnClickListener { showAddCategory() }
            btnAddCategoryEmpty.setOnClickListener { showAddCategory() }

            btnExpenseToggle.setOnClickListener { showExpenseCategories() }
            btnIncomeToggle.setOnClickListener { showIncomeCategories() }

            // Setup RecyclerViews
            recyclerViewExpenseCategory.apply {
                layoutManager = GridLayoutManager(context, 2)
                addItemDecoration(GridSpacingItemDecoration(2, 8, true))
            }

            recyclerViewIncomeCategory.apply {
                layoutManager = GridLayoutManager(context, 2)
                addItemDecoration(GridSpacingItemDecoration(2, 8, true))
            }
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            filterCategories(text?.toString() ?: "")
        }
    }

    private fun filterCategories(query: String) {
        expenseAdapter?.filter(query)
        incomeAdapter?.filter(query)
        updateEmptyState()
    }

    private fun loadCategories() {
        categoryViewModel.loadCategories()
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            val (expenseCategories, incomeCategories) = categories.partition { it.categoryType == "expense" }

            expenseAdapter = TransactionSelectCategoryAdapter(expenseCategories) { category ->
                viewModel.setCategory(category)
                findNavController().popBackStack()
            }

            incomeAdapter = TransactionSelectCategoryAdapter(incomeCategories) { category ->
                viewModel.setCategory(category)
                findNavController().popBackStack()
            }

            binding.recyclerViewExpenseCategory.adapter = expenseAdapter
            binding.recyclerViewIncomeCategory.adapter = incomeAdapter

            updateEmptyState()
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
            val currentList = if (recyclerViewExpenseCategory.visibility == View.VISIBLE) {
                expenseAdapter?.currentList
            } else {
                incomeAdapter?.currentList
            }

            emptyState.visibility = if (currentList.isNullOrEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun showAddCategory() {
        findNavController().navigate(R.id.navigation_category_add_new)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Grid spacing decoration
    private inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : ItemDecoration() {

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

                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }
}