package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.HomeListItems
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeMainFragment : Fragment() {
    companion object {
        private const val MAX_ITEMS = 3
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeMainViewModel by activityViewModels()

    // Single unified adapter for all sections
    private val homeAdapter by lazy {
        HomeAdapter(
            onWalletClick = { wallet ->
                // TODO: Implement wallet click handling
            },
            onTransactionClick = { transaction ->
                // TODO: Implement transaction click handling
            },
            onCategoryClick = { category ->
                // TODO: Implement category click handling
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
            // Setup RecyclerViews with the unified adapter
            recyclerViewHomeWalletOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = homeAdapter
            }
            
            recyclerViewHomeTransactionOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = homeAdapter
            }
            
            recyclerViewHomeCategoryOverview.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = homeAdapter
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
                        handleSectionState(
                            state = state,
                            recyclerView = binding.recyclerViewHomeWalletOverview,
                            progressBar = binding.progressBarWallets,
                            emptyText = binding.txtEmptyWallets,
                            section = Section.WALLETS
                        )
                    }
                }
                launch {
                    viewModel.transactionsState.collect { state ->
                        handleSectionState(
                            state = state,
                            recyclerView = binding.recyclerViewHomeTransactionOverview,
                            progressBar = binding.progressBarTransactions,
                            emptyText = binding.txtEmptyTransactions,
                            section = Section.TRANSACTIONS
                        )
                    }
                }
                launch {
                    viewModel.categoriesState.collect { state ->
                        handleSectionState(
                            state = state,
                            recyclerView = binding.recyclerViewHomeCategoryOverview,
                            progressBar = binding.progressBarCategories,
                            emptyText = binding.txtEmptyCategories,
                            section = Section.CATEGORIES
                        )
                    }
                }
            }
        }
    }

    private fun handleSectionState(
        state: Any,
        recyclerView: RecyclerView,
        progressBar: ProgressBar,
        emptyText: TextView,
        section: Section
    ) {
        when (state) {
            is HomeMainViewModel.WalletState.Loading,
            is HomeMainViewModel.TransactionState.Loading,
            is HomeMainViewModel.CategoryState.Loading -> {
                recyclerView.isVisible = false
                emptyText.isVisible = false
                progressBar.isVisible = true
            }
            is HomeMainViewModel.WalletState.Success -> {
                val wallets = state.wallets
                progressBar.isVisible = false
                if (wallets.isEmpty()) {
                    recyclerView.isVisible = false
                    emptyText.isVisible = true
                    return
                }

                recyclerView.isVisible = true
                emptyText.isVisible = false
                
                val transactions = viewModel.getCachedTransactions()
                val items = wallets.take(MAX_ITEMS).map { wallet ->
                    val walletTransactions = transactions.filter { it.wallet.id == wallet.id }
                    val mostRecentDate = walletTransactions.minOfOrNull { it.date } ?: System.currentTimeMillis()
                    
                    HomeListItems.HomeWalletItem(
                        wallet = wallet,
                        walletIcon = R.drawable.ic_ui_wallet,
                        relativeDate = getRelativeDate(mostRecentDate)
                    )
                }
                homeAdapter.submitListForSection(section, items)
            }
            is HomeMainViewModel.TransactionState.Success -> {
                val transactions = state.transactions
                progressBar.isVisible = false
                if (transactions.isEmpty()) {
                    recyclerView.isVisible = false
                    emptyText.isVisible = true
                    return
                }

                recyclerView.isVisible = true
                emptyText.isVisible = false
                
                val items = transactions.take(MAX_ITEMS).map { transaction ->
                    HomeListItems.HomeTransactionItem(
                        transaction = transaction,
                        relativeDate = getRelativeDate(transaction.date)
                    )
                }
                homeAdapter.submitListForSection(section, items)
            }
            is HomeMainViewModel.CategoryState.Success -> {
                val categories = state.categories
                progressBar.isVisible = false
                if (categories.isEmpty()) {
                    recyclerView.isVisible = false
                    emptyText.isVisible = true
                    return
                }

                recyclerView.isVisible = true
                emptyText.isVisible = false
                
                val transactions = viewModel.getCachedTransactions()
                val items = categories.take(MAX_ITEMS).map { category ->
                    val categoryTransactions = transactions.filter { it.category.id == category.id }
                    val transactionCount = categoryTransactions.size
                    val totalAmount = categoryTransactions.sumOf { it.amount }
                    val formattedAmount = String.format("R %.2f", totalAmount)
                    val mostRecentDate = categoryTransactions.maxOfOrNull { it.date } ?: System.currentTimeMillis()
                    
                    HomeListItems.HomeCategoryItem(
                        category = category,
                        transactionCount = transactionCount,
                        amount = formattedAmount,
                        relativeDate = getRelativeDate(mostRecentDate)
                    )
                }
                homeAdapter.submitListForSection(section, items)
            }
            is HomeMainViewModel.WalletState.Error,
            is HomeMainViewModel.TransactionState.Error,
            is HomeMainViewModel.CategoryState.Error -> {
                val errorMessage = when (state) {
                    is HomeMainViewModel.WalletState.Error -> state.message
                    is HomeMainViewModel.TransactionState.Error -> state.message
                    is HomeMainViewModel.CategoryState.Error -> state.message
                    else -> "Unknown error"
                }
                recyclerView.isVisible = false
                emptyText.isVisible = true
                emptyText.text = errorMessage
                showError(errorMessage)
            }
        }
    }

    private enum class Section {
        WALLETS, TRANSACTIONS, CATEGORIES
    }

    private fun getRelativeDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            else -> {
                val date = Date(timestamp)
                val format = SimpleDateFormat("MMM dd", Locale.getDefault())
                format.format(date)
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Only force refresh if cache is stale
        viewModel.refreshData(forceRefresh = viewModel.isCacheStale())
    }
}
