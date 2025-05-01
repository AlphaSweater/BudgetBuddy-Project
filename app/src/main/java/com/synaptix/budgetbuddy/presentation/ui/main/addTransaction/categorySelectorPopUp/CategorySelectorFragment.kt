package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.categorySelectorPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.databinding.FragmentSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.AddTransactionViewModel

class CategorySelectorFragment : Fragment() {

    private var _binding: FragmentSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by activityViewModels()

    //test data
    private val categoryList = listOf(
        Category(1,1,"Food And Drinks", "Expense", "@drawable/baseline_fastfood_24", "@color/cat_yellow"),
        Category(1,1,"Transport", "Expense", "@drawable/baseline_local_gas_station_24", "@color/cat_dark_blue"),
        Category(1,1,"Shopping", "Expense", "@drawable/baseline_shopping_bag_24", "cat_dark_green"),
        Category(1,1,"Education", "Expense", "@drawable/ic_add_alert_24", "@color/cat_orange"),
        Category(1,1,"Rent And Mortgage", "Expense", "@drawable/ic_add_alert_24", "@color/cat_light_pink")
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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val categoryAdapter = CategoryAdapter(categoryList) { category ->
            viewModel.categoryId.value = category.categoryId
        }

        binding.recyclerViewExpenseCategory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}