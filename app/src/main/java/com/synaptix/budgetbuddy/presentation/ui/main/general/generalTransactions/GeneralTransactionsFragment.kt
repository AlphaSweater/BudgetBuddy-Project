package com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentGeneralTransactionsBinding
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports.ReportListItems
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class GeneralTransactionsFragment : Fragment() {
    private var _binding: FragmentGeneralTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralTransactionsViewModel by viewModels()
    private lateinit var transactionsAdapter: GeneralTransactionsAdapter
    private var currentDateRange: Pair<Long, Long>? = null

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
        setupRecyclerView()
        setupClickListeners()
        setupWalletDropdown()
        setupDateSelection()
        setupViewSwitcher()
        observeStates()
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
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnClearDate.setOnClickListener {
            currentDateRange = null
            updateDateRangeText()
            viewModel.clearDateRange()
        }
    }

    private fun setupWalletDropdown() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.walletsState.collect { walletState ->
                    when (walletState) {
                        is GeneralTransactionsViewModel.WalletState.Success -> {
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

                                // Set default selection to "All Wallets"
                                binding.autoCompleteWallet.setText("All Wallets", false)
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
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        
        // Default to last 30 days
        val defaultEndDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val defaultStartDate = calendar.timeInMillis

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

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect wallet state
                launch {
                    viewModel.walletsState.collect { walletState ->
                        when (walletState) {
                            is GeneralTransactionsViewModel.WalletState.Success -> {
                                // Wallet state is handled in setupWalletDropdown
                            }
                            else -> {}
                        }
                    }
                }

                // Collect selected wallet changes
                launch {
                    viewModel.selectedWallet.collect { selectedWallet ->
                        updateTransactionsForSelectedWallet(selectedWallet)
                    }
                }

                // Collect transactions state
                launch {
                    viewModel.transactionsState.collect { state ->
                        when (state) {
                            is GeneralTransactionsViewModel.TransactionState.Success -> {
                                updateTransactionsForSelectedWallet(viewModel.selectedWallet.value)
                            }
                            is GeneralTransactionsViewModel.TransactionState.Error -> {
                                binding.recyclerViewGeneralTransactions.visibility = View.GONE
                                // Show error state
                            }
                            is GeneralTransactionsViewModel.TransactionState.Loading -> {
                                // Show loading state
                            }
                            is GeneralTransactionsViewModel.TransactionState.Empty -> {
                                binding.recyclerViewGeneralTransactions.visibility = View.GONE
                                transactionsAdapter.submitList(emptyList())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateTransactionsForSelectedWallet(selectedWallet: Wallet?) {
        val currentState = viewModel.transactionsState.value
        if (currentState is GeneralTransactionsViewModel.TransactionState.Success) {
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
        binding.tvCurrencyTotal.text = String.format("R %.2f", totalBalance)
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}