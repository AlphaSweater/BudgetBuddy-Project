package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.*
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class HomeMainFragment : Fragment() {
    companion object {
        private const val MAX_ITEMS = 3
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeMainViewModel by activityViewModels()

    // Initialize adapters once and reuse them
    private val walletAdapter by lazy {
        HomeAdapter(
            onWalletClick = { wallet ->
                // TODO: Implement wallet click handling:
                // 1. Navigate to wallet details screen
                // 2. Pass wallet data using Safe Args:
                //    - wallet name
                //    - wallet balance
                // 3. Show wallet transactions for this specific wallet
            }
        )
    }

    private val transactionAdapter by lazy {
        HomeAdapter(
            onTransactionClick = { transaction ->
                // TODO: Implement transaction click handling:
                // 1. Navigate to transaction details screen
                // 2. Pass transaction data using Safe Args:
                //    - transaction amount
                //    - category details
                //    - wallet details
                //    - date and notes
                // 3. Allow editing and viewing of transaction
            }
        )
    }

    private val categoryAdapter by lazy {
        HomeAdapter(
            onCategoryClick = { category ->
                // TODO: Implement category click handling:
                // 1. Navigate to category details screen
                // 2. Pass category data using Safe Args:
                //    - category name
                //    - category icon
                //    - category color
                // 3. Show transactions for this specific category
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeStates()
    }

    private fun setupViews() {
        binding.apply {
            //editTextDate2.setOnClickListener { openDateRangePicker() }

            // Setup RecyclerViews with their adapters
            recyclerViewHomeWalletOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = walletAdapter
            }
            
            recyclerViewHomeTransactionOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = transactionAdapter
            }
            
            recyclerViewHomeCategoryOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryAdapter
            }

            // Setup click listeners
            txtViewAllWallets.setOnClickListener {
                // TODO: Navigate to all wallets
            }
            
            txtViewAllCategories.setOnClickListener {
                // TODO: Navigate to all categories
            }
            
            txtViewAllTransactions.setOnClickListener {
                // TODO: Navigate to all transactions
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.walletsState.collect { state ->
                        handleWalletsState(state)
                    }
                }
                launch {
                    viewModel.transactionsState.collect { state ->
                        handleTransactionsState(state)
                    }
                }
                launch {
                    viewModel.categoriesState.collect { state ->
                        handleCategoriesState(state)
                    }
                }
            }
        }
    }

    private fun handleWalletsState(state: HomeMainViewModel.WalletState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.WalletState.Loading -> {
                    recyclerViewHomeWalletOverview.isVisible = false
                    txtEmptyWallets.isVisible = false
                    // TODO: Show loading indicator
                }
                is HomeMainViewModel.WalletState.Success -> {
                    val wallets = state.wallets
                    if (wallets.isEmpty()) {
                        recyclerViewHomeWalletOverview.isVisible = false
                        txtEmptyWallets.isVisible = true
                        return
                    }

                    recyclerViewHomeWalletOverview.isVisible = true
                    txtEmptyWallets.isVisible = false
                    
                    val walletItems = wallets.take(MAX_ITEMS).map { wallet ->
                        HomeWalletItem(
                            walletName = (wallet).walletName,
                            walletIcon = R.drawable.baseline_shopping_bag_24,
                            walletBalance = wallet.walletBalance,
                            relativeDate = "Recent"
                        )
                    }
                    walletAdapter.submitList(walletItems)
                }
                is HomeMainViewModel.WalletState.Error -> {
                    recyclerViewHomeWalletOverview.isVisible = false
                    txtEmptyWallets.isVisible = true
                    txtEmptyWallets.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun handleTransactionsState(state: HomeMainViewModel.TransactionState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.TransactionState.Loading -> {
                    recyclerViewHomeTransactionOverview.isVisible = false
                    txtEmptyTransactions.isVisible = false
                    // TODO: Show loading indicator
                }
                is HomeMainViewModel.TransactionState.Success -> {
                    val transactions = state.transactions
                    if (transactions.isEmpty()) {
                        recyclerViewHomeTransactionOverview.isVisible = false
                        txtEmptyTransactions.isVisible = true
                        return
                    }

                    recyclerViewHomeTransactionOverview.isVisible = true
                    txtEmptyTransactions.isVisible = false
                    
                    val transactionItems = transactions.take(MAX_ITEMS).map { transaction ->
                        transaction.let { tx ->
                            TransactionItem(
                                categoryName = tx.category?.categoryName ?: "",
                                categoryIcon = tx.category?.categoryIcon ?: R.drawable.baseline_shopping_bag_24,
                                categoryColour = tx.category?.categoryColor ?: R.color.dark_background,
                                amount = tx.amount,
                                walletName = tx.wallet?.walletName ?: "",
                                note = tx.note,
                                relativeDate = tx.date
                            )
                        }
                    }
                    transactionAdapter.submitList(transactionItems)
                }
                is HomeMainViewModel.TransactionState.Error -> {
                    recyclerViewHomeTransactionOverview.isVisible = false
                    txtEmptyTransactions.isVisible = true
                    txtEmptyTransactions.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun handleCategoriesState(state: HomeMainViewModel.CategoryState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.CategoryState.Loading -> {
                    recyclerViewHomeCategoryOverview.isVisible = false
                    txtEmptyCategories.isVisible = false
                    // TODO: Show loading indicator
                }
                is HomeMainViewModel.CategoryState.Success -> {
                    val categories = state.categories
                    if (categories.isEmpty()) {
                        recyclerViewHomeCategoryOverview.isVisible = false
                        txtEmptyCategories.isVisible = true
                        return
                    }

                    recyclerViewHomeCategoryOverview.isVisible = true
                    txtEmptyCategories.isVisible = false
                    
                    val categoryItems = categories.take(MAX_ITEMS).map { category ->
                        category.let { cat ->
                            CategoryItems(
                                categoryName = cat.categoryName,
                                categoryIcon = cat.categoryIcon,
                                categoryColour = cat.categoryColor,
                                transactionCount = 0, // TODO: Calculate actual count
                                amount = "0.00", // TODO: Calculate actual amount
                                relativeDate = "Recent"
                            )
                        }
                    }
                    categoryAdapter.submitList(categoryItems)
                }
                is HomeMainViewModel.CategoryState.Error -> {
                    recyclerViewHomeCategoryOverview.isVisible = false
                    txtEmptyCategories.isVisible = true
                    txtEmptyCategories.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
}
