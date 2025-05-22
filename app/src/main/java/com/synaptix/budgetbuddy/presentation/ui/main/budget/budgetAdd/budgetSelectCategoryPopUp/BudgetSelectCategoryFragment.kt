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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.usecase.auth.GetUserIdUseCase
import com.synaptix.budgetbuddy.databinding.FragmentBudgetSelectCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.BudgetAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BudgetSelectCategoryFragment : Fragment() {

    // Dependency Injection for getting user ID
    @Inject
    lateinit var getUserIdUseCase: GetUserIdUseCase

    private var _binding: FragmentBudgetSelectCategoryBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel between this and BudgetAddFragment
    private val viewModel: BudgetAddViewModel by activityViewModels()

    // ViewModel specific to this fragment
    private val categoryViewModel: BudgetSelectCategoryViewModel by viewModels()

    private val categoryAdapter by lazy {
        BudgetSelectCategoryAdapter { selectedCategories ->
            viewModel.selectedCategories.value = selectedCategories
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
        setupRecyclerView()
        setupClickListeners()
        observeCategories()
    }

    // Setup for RecyclerViews with LinearLayoutManager
    private fun setupRecyclerView() {
        binding.recyclerViewExpenseCategory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }

        // Initialize with selected categories from ViewModel
        val initialSelected = viewModel.selectedCategories.value ?: emptyList()
        categoryAdapter.submitList(emptyList(), initialSelected)
    }

    // Handles button click listeners
    private fun setupClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack() // Go back in navigation stack
        }

        binding.btnAddCategory.setOnClickListener {
            showAddCategory() // Navigate to add category screen
        }
    }

    // Navigates to category addition screen
    private fun showAddCategory() {
        findNavController().navigate(R.id.navigation_category_add_new)
    }

    // Clears binding to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.loadCategories()
            categoryViewModel.categories.collectLatest { categories ->
                val selectedCategories = viewModel.selectedCategories.value ?: emptyList()
                categoryAdapter.submitList(categories, selectedCategories)
            }
        }
    }
}
