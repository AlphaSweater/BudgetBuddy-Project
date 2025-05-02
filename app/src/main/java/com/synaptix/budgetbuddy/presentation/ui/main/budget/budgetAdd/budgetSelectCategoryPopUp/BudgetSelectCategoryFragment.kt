package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectCategoryPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.databinding.FragmentBudgetSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.BudgetAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BudgetSelectCategoryFragment : Fragment() {

    @Inject
    lateinit var getUserIdUseCase: GetUserIdUseCase
    private var _binding: FragmentBudgetSelectCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetAddViewModel by activityViewModels()
    private val categoryViewmodel: BudgetSelectCategoryViewModel by viewModels()


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
        setupRecyclerViews()
        setupOnClickListeners()
        showExpenseCategories()
        instantiateDBS()
    }


    private fun setupRecyclerViews() {
        binding.recyclerViewExpenseCategory.layoutManager = LinearLayoutManager(requireContext())
    }
    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategory()
        }
    }

    private fun showExpenseCategories() {
        binding.recyclerViewExpenseCategory.visibility = View.VISIBLE
    }

    private fun showAddCategory() {
        findNavController().navigate(R.id.navigation_category_add_new)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun instantiateDBS() {
        categoryViewmodel.loadCategories()

        categoryViewmodel.categories.observe(viewLifecycleOwner) { categories ->

            val (expenseCategories, incomeCategories) = categories.partition { it.categoryType == "expense" }

            binding.recyclerViewExpenseCategory.adapter = BudgetSelectCategoryAdapter(expenseCategories) { category ->
                viewModel.category.value = category
                findNavController().popBackStack()
            }
        }
    }
}