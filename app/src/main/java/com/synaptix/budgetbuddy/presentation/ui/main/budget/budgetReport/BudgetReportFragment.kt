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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.databinding.FragmentBudgetReportBinding
import dagger.hilt.android.AndroidEntryPoint
//
@AndroidEntryPoint
class BudgetReportFragment : Fragment() {
//
//    private var _binding: FragmentBudgetReportBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: BudgetReportViewModel by viewModels()
//
//    private val transactionAdapter by lazy {
//        BudgetReportAdapter(
//            onTransactionClick = { transaction ->
//                // TODO: Handle transaction click
//            }
//        )
//    }
//
//    private val categoryAdapter by lazy {
//        BudgetReportAdapter(
//            onCategoryClick = { category ->
//                // TODO: Handle category click
//            }
//        )
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentBudgetReportBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupUI()
//        observeViewModel()
//    }
//
//    private fun setupUI() {
//        setupRecyclerViews()
//        setupClickListeners()
//    }
//
//    private fun setupRecyclerViews() {
//        // Setup transactions recycler
//        binding.recyclerViewTransactions.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = transactionAdapter
//        }
//
//        // Setup categories recycler
//        binding.recyclerViewCategories.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = categoryAdapter
//        }
//    }
//
//    private fun setupClickListeners() {
//        binding.btnGoBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//    }
//
//    private fun observeViewModel() {
//        viewModel.selectedBudget.observe(viewLifecycleOwner) { budget ->
//            // Update UI with budget details
//            binding.textBudgetName.text = budget.budgetName
//            binding.textBudgetAmount.text = "R ${budget.amount}"
//            binding.textBudgetSpent.text = "R ${budget.spent}"
//        }
//
//        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
//            transactionAdapter.submitList(transactions)
//        }
//
//        viewModel.categories.observe(viewLifecycleOwner) { categories ->
//            categoryAdapter.submitList(categories)
//        }
//
//        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
//            errorMessage?.let {
//                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}
