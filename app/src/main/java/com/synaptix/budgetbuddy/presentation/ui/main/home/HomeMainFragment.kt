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

    private lateinit var homeAdapter: HomeAdapter

    private val TAG = "HomeMainFragment"

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

            // Setup RecyclerViews
            recyclerViewHomeWalletOverview.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewHomeTransactionOverview.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewHomeCategoryOverview.layoutManager = LinearLayoutManager(requireContext())

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

    private fun handleWalletsState(state: HomeMainViewModel.UiState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.UiState.Loading -> {
                    recyclerViewHomeWalletOverview.isVisible = false
                    txtEmptyWallets.isVisible = false
                    // TODO: Show loading indicator
                }
                is HomeMainViewModel.UiState.Success<*> -> {
                    val wallets = state.data as List<*>
                    if (wallets.isEmpty()) {
                        recyclerViewHomeWalletOverview.isVisible = false
                        txtEmptyWallets.isVisible = true
                        return
                    }

                    recyclerViewHomeWalletOverview.isVisible = true
                    txtEmptyWallets.isVisible = false
                    
                    val walletItems = wallets.take(MAX_ITEMS).map { wallet ->
                        HomeWalletItem(
                            walletName = (wallet as? com.synaptix.budgetbuddy.core.model.Wallet)?.walletName ?: "",
                            walletIcon = R.drawable.baseline_shopping_bag_24,
                            walletBalance = (wallet as? com.synaptix.budgetbuddy.core.model.Wallet)?.walletBalance ?: 0.0,
                            relativeDate = "Recent"
                        )
                    }
                    setupWalletAdapter(walletItems)
                }
                is HomeMainViewModel.UiState.Error -> {
                    recyclerViewHomeWalletOverview.isVisible = false
                    txtEmptyWallets.isVisible = true
                    txtEmptyWallets.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun handleTransactionsState(state: HomeMainViewModel.UiState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.UiState.Loading -> {
                    recyclerViewHomeTransactionOverview.isVisible = false
                    txtEmptyTransactions.isVisible = false
                    // TODO: Show loading indicator
                }
                is HomeMainViewModel.UiState.Success<*> -> {
                    val transactions = state.data as List<*>
                    if (transactions.isEmpty()) {
                        recyclerViewHomeTransactionOverview.isVisible = false
                        txtEmptyTransactions.isVisible = true
                        return
                    }

                    recyclerViewHomeTransactionOverview.isVisible = true
                    txtEmptyTransactions.isVisible = false
                    
                    val transactionItems = transactions.take(MAX_ITEMS).mapNotNull { transaction ->
                        (transaction as? com.synaptix.budgetbuddy.core.model.Transaction)?.let { tx ->
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
                    setupTransactionAdapter(transactionItems)
                }
                is HomeMainViewModel.UiState.Error -> {
                    recyclerViewHomeTransactionOverview.isVisible = false
                    txtEmptyTransactions.isVisible = true
                    txtEmptyTransactions.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun handleCategoriesState(state: HomeMainViewModel.UiState) {
        binding.apply {
            when (state) {
                is HomeMainViewModel.UiState.Loading -> {
                    recyclerViewHomeCategoryOverview.isVisible = false
                    txtEmptyCategories.isVisible = false
                    // TODO: Show loading indicator
                }
                is HomeMainViewModel.UiState.Success<*> -> {
                    val categories = state.data as List<*>
                    if (categories.isEmpty()) {
                        recyclerViewHomeCategoryOverview.isVisible = false
                        txtEmptyCategories.isVisible = true
                        return
                    }

                    recyclerViewHomeCategoryOverview.isVisible = true
                    txtEmptyCategories.isVisible = false
                    
                    val categoryItems = categories.take(MAX_ITEMS).mapNotNull { category ->
                        (category as? com.synaptix.budgetbuddy.core.model.Category)?.let { cat ->
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
                    setupCategoryAdapter(categoryItems)
                }
                is HomeMainViewModel.UiState.Error -> {
                    recyclerViewHomeCategoryOverview.isVisible = false
                    txtEmptyCategories.isVisible = true
                    txtEmptyCategories.text = state.message
                    showError(state.message)
                }
            }
        }
    }

    private fun setupWalletAdapter(items: List<HomeWalletItem>) {
        binding.recyclerViewHomeWalletOverview.adapter = HomeAdapter(
            items = items,
            onWalletClick = { wallet ->
                // TODO: Navigate to wallet details
            }
        )
    }

    private fun setupTransactionAdapter(items: List<TransactionItem>) {
        binding.recyclerViewHomeTransactionOverview.adapter = HomeAdapter(
            items = items,
            onTransactionClick = { transaction ->
                // TODO: Navigate to transaction details
            }
        )
    }

    private fun setupCategoryAdapter(items: List<CategoryItems>) {
        binding.recyclerViewHomeCategoryOverview.adapter = HomeAdapter(
            items = items,
            onCategoryClick = { category ->
                // TODO: Navigate to category details
            }
        )
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

//    private fun openDateRangePicker() {
//        val picker = MaterialDatePicker.Builder.dateRangePicker()
//            .setTitleText("Select Date Range")
//            .build()
//
//        picker.addOnPositiveButtonClickListener { selection ->
//            val startDate = selection.first
//            val endDate = selection.second
//
//            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//
//            calendar.timeInMillis = startDate
//            val formattedStartDate = dateFormat.format(calendar.time)
//
//            calendar.timeInMillis = endDate
//            val formattedEndDate = dateFormat.format(calendar.time)
//
//            binding.editTextDate2.setText("$formattedStartDate - $formattedEndDate")
//
//            viewModel.selectedStartDate = formattedStartDate
//            viewModel.selectedEndDate = formattedEndDate
//        }
//
//        picker.show(parentFragmentManager, "MATERIAL_DATE_RANGE_PICKER")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
