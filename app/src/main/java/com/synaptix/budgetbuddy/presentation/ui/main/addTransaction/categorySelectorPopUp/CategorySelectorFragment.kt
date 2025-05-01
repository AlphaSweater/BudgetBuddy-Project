package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.categorySelectorPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.databinding.FragmentSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.AddTransactionViewModel

class CategorySelectorFragment : Fragment() {

    private var _binding: FragmentSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by activityViewModels()

    //test data
    private val expenseCategories = listOf(
        Category(1, 1, "Food And Drinks", "Expense", R.drawable.baseline_fastfood_24, R.color.cat_yellow),
        Category(2, 1, "Transport", "Expense", R.drawable.baseline_local_gas_station_24, R.color.cat_dark_blue),
        Category(3, 1, "Shopping", "Expense", R.drawable.baseline_shopping_bag_24, R.color.cat_dark_green),
        Category(4, 1, "Education", "Expense", R.drawable.ic_add_alert_24, R.color.cat_orange),
        Category(5, 1, "Rent And Mortgage", "Expense", R.drawable.ic_add_alert_24, R.color.cat_light_pink)
    )

    // sample income data
    private val incomeCategories = listOf(
        Category(6, 1, "Salary", "Income", R.drawable.ic_account_balance_wallet_24, R.color.cat_light_green),
        Category(7, 1, "Freelance", "Income", R.drawable.ic_attach_money_24, R.color.cat_light_blue)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupOnClickListeners()
        showExpenseCategories()
    }

    private fun setupRecyclerViews() {
        binding.recyclerViewExpenseCategory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CategoryAdapter(expenseCategories) { category ->
                viewModel.categoryId.value = category.categoryId
            }
        }

        binding.recyclerViewIncomeCategory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CategoryAdapter(incomeCategories) { category ->
                viewModel.categoryId.value = category.categoryId
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnExpenseToggle.setOnClickListener {
            showExpenseCategories()
        }

        binding.btnIncomeToggle.setOnClickListener {
            showIncomeCategories()
        }
    }

    private fun showExpenseCategories() {
        binding.recyclerViewExpenseCategory.visibility = View.VISIBLE
        binding.recyclerViewIncomeCategory.visibility = View.GONE

        highlightToggle(binding.btnExpenseToggle, binding.btnIncomeToggle)
    }

    private fun showIncomeCategories() {
        binding.recyclerViewExpenseCategory.visibility = View.GONE
        binding.recyclerViewIncomeCategory.visibility = View.VISIBLE

        highlightToggle(binding.btnIncomeToggle, binding.btnExpenseToggle)
    }

    private fun highlightToggle(selected: TextView, unselected: TextView) {
        selected.setBackgroundResource(R.drawable.toggle_selected)
        unselected.setBackgroundResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}