package com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.databinding.FragmentGeneralTransactionsBinding
import com.synaptix.budgetbuddy.presentation.ui.main.general.GeneralViewModel
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports.ReportListItems
import com.synaptix.budgetbuddy.core.util.DateUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragment for displaying general transactions.
 * 
 * This fragment is responsible for:
 * 1. Displaying transaction list
 * 2. Handling wallet selection
 * 3. Managing date range filtering
 * 4. Handling user interactions
 * 
 * The fragment uses:
 * - ViewBinding for view access
 * - ViewModel for data management
 * - Coroutines for asynchronous operations
 */
@AndroidEntryPoint
class GeneralTransactionsFragment : Fragment() {

    //================================================================================
    // Properties
    //================================================================================
    private var _binding: FragmentGeneralTransactionsBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share the ViewModel between fragments
    private val viewModel: GeneralViewModel by activityViewModels()
    private lateinit var transactionsAdapter: GeneralTransactionsAdapter
    private var currentDateRange: Pair<Long, Long>? = null

    //================================================================================
    // Lifecycle Methods
    //================================================================================
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeStates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //================================================================================
    // Setup Methods
    //================================================================================
    private fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
        setupWalletDropdown()
        setupDateSelection()
        setupViewSwitcher()
    }

    private fun setupRecyclerView() {
        transactionsAdapter = GeneralTransactionsAdapter { transaction ->
            // Handle transaction click
            // findNavController().navigate(...)
        }

        binding.recyclerViewGeneralTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnClearDate.setOnClickListener {
                currentDateRange = null
                updateDateRangeText()
                viewModel.clearDateRange()
            }
        }
    }

    private fun setupWalletDropdown() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.walletState.collectLatest { walletState ->
                    when (walletState) {
                        is GeneralViewModel.WalletState.Success -> {
                            val wallets = walletState.wallets
                            if (wallets.isNotEmpty()) {
                                // Create a list with "All Wallets" as the first item
                                val walletNames = listOf("All Wallets") + wallets.map { it.name }

                                val adapter = ArrayAdapter(
                                    requireContext(),
                                    R.layout.item_dropdown_item,
                                    walletNames
                                )

                                binding.autoCompleteWallet.setAdapter(adapter)

                                // Set up the item selected listener
                                binding.autoCompleteWallet.setOnItemClickListener { _, _, position, _ ->
                                    val selectedWallet = if (position == 0) {
                                        null // "All Wallets" selected
                                    } else {
                                        wallets[position - 1] // Adjust index since we added "All Wallets" at position 0
                                    }
                                    viewModel.selectWallet(selectedWallet)
                                }

                                // Set initial selection based on persisted wallet
                                viewModel.selectedWallet.value?.let { selectedWallet ->
                                    val position = wallets.indexOf(selectedWallet) + 1 // +1 because of "All Wallets"
                                    if (position > 0) {
                                        binding.autoCompleteWallet.setText(walletNames[position], false)
                                    } else {
                                        binding.autoCompleteWallet.setText("All Wallets", false)
                                    }
                                } ?: run {
                                    binding.autoCompleteWallet.setText("All Wallets", false)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupDateSelection() {
        binding.btnSelectDate.setOnClickListener {
            showDateRangePicker()
        }

        // Update date range text based on persisted range
        viewModel.dateRange.value?.let { range ->
            currentDateRange = range.start to range.endInclusive
            updateDateRangeText()
        }
    }

    private fun setupViewSwitcher() {
        binding.apply {
            // Set initial state - Transactions view is active
            btnTransactionsView.setBackgroundResource(R.drawable.toggle_selected)
            btnReportsView.setBackgroundResource(android.R.color.transparent)

            // Set up click listeners
            btnReportsView.setOnClickListener {
                findNavController().navigate(R.id.navigation_general_reports)
            }

            btnTransactionsView.setOnClickListener {
                // Already in transactions view, do nothing
            }
        }
    }

    //================================================================================
    // State Observation
    //================================================================================
    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect wallet state
                launch {
                    viewModel.walletState.collectLatest { walletState ->
                        when (walletState) {
                            is GeneralViewModel.WalletState.Success -> {
                                // Wallet state is handled in setupWalletDropdown
                            }
                            else -> {}
                        }
                    }
                }

                // Collect selected wallet changes
                launch {
                    viewModel.selectedWallet.collectLatest { selectedWallet ->
                        updateTransactionsForSelectedWallet(selectedWallet)
                    }
                }

                // Collect date range changes
                launch {
                    viewModel.dateRange.collectLatest { range ->
                        range?.let {
                            currentDateRange = it.start to it.endInclusive
                            updateDateRangeText()
                        }
                    }
                }

                // Collect transactions state
                launch {
                    viewModel.transactionsState.collectLatest { state ->
                        when (state) {
                            is GeneralViewModel.TransactionState.Success -> {
                                updateTransactionsForSelectedWallet(viewModel.selectedWallet.value)
                            }
                            is GeneralViewModel.TransactionState.Error -> {
                                binding.recyclerViewGeneralTransactions.visibility = View.GONE
                                // Show error state
                            }
                            is GeneralViewModel.TransactionState.Loading -> {
                                // Show loading state
                            }
                            is GeneralViewModel.TransactionState.Empty -> {
                                binding.recyclerViewGeneralTransactions.visibility = View.GONE
                                transactionsAdapter.submitList(emptyList())
                            }
                        }
                    }
                }
            }
        }
    }

    //================================================================================
    // UI Update Methods
    //================================================================================
    private fun updateTransactionsForSelectedWallet(selectedWallet: Wallet?) {
        val currentState = viewModel.transactionsState.value
        if (currentState is GeneralViewModel.TransactionState.Success) {
            val filteredTransactions = if (selectedWallet != null) {
                currentState.transactions.filter { it.wallet.id == selectedWallet.id }
            } else {
                currentState.transactions
            }.sortedByDescending { it.date }

            val items = filteredTransactions.map { transaction ->
                ReportListItems.ReportTransactionItem(
                    transaction = transaction,
                    relativeDate = formatDate(transaction.date)
                )
            }

            transactionsAdapter.submitList(items)
            updateTotalBalance(filteredTransactions)
            binding.recyclerViewGeneralTransactions.visibility = View.VISIBLE
        }
    }

    private fun updateTotalBalance(transactions: List<Transaction>) {
        val totalBalance = transactions.sumOf { it.amount }
        binding.tvCurrencyTotal.text = CurrencyUtil.formatWithSymbol(totalBalance)
    }

    //================================================================================
    // Date Range Methods
    //================================================================================
    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        
        // Use persisted date range if available, otherwise default to current month
        val (defaultStartDate, defaultEndDate) = viewModel.dateRange.value?.let { range ->
            range.start to range.endInclusive
        } ?: DateUtil.getCurrentMonthRange()

        // Create and show the date range picker dialog
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .setSelection(
                androidx.core.util.Pair(
                    defaultStartDate,
                    defaultEndDate
                )
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first ?: return@addOnPositiveButtonClickListener
            val endDate = selection.second ?: return@addOnPositiveButtonClickListener
            currentDateRange = startDate to endDate
            updateDateRangeText()
            viewModel.setDateRange(startDate, endDate)
        }

        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun updateDateRangeText() {
        binding.tvDateRange.text = if (currentDateRange != null) {
            val startDate = Date(currentDateRange!!.first)
            val endDate = Date(currentDateRange!!.second)
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        } else {
            "Select Date"
        }
    }

    //================================================================================
    // Utility Methods
    //================================================================================
    private fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, date) -> "Today"
            isYesterday(now, date) -> "Yesterday"
            else -> {
                val day = date.get(Calendar.DAY_OF_MONTH)
                val month = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                "$month $day"
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_MONTH, -1)
        }
        return isSameDay(yesterday, date)
    }
}