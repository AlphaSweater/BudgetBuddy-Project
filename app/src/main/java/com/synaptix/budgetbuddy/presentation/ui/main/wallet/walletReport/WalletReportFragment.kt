package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletReport

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.usecase.main.transaction.GetTransactionsUseCase
import com.synaptix.budgetbuddy.databinding.FragmentWalletReportBinding
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports.ReportListItems
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class WalletReportFragment : Fragment() {
    private var _binding: FragmentWalletReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletReportViewModel by viewModels()
    private lateinit var transactionsAdapter: WalletReportAdapter

    private var walletId: String? = null

    @Inject
    lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            walletId = it.getString("walletId")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("WalletReportFragment: onViewCreated")

        // Log the received wallet ID
        println("WalletReportFragment: Received wallet ID: $walletId")

        val currentUser = auth.currentUser
        if (currentUser == null) {
            println("WalletReportFragment: User not authenticated")
            findNavController().popBackStack()
            return
        }

        val walletId = walletId ?: run {
            println("WalletReportFragment: No wallet ID provided")
            findNavController().popBackStack()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Log before calling loadWalletTransactions
        println("WalletReportFragment: Calling loadWalletTransactions with walletId: $walletId")
        viewModel.loadWalletTransactions(walletId!!)
    }

    private fun setupRecyclerView() {
        transactionsAdapter = WalletReportAdapter { transaction ->
            // Handle transaction click if needed
        }

        binding.recyclerViewWalletReport.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transactionsState.collect { state ->
                        println("WalletReportFragment: Received state: $state")
                        when (state) {
                            is WalletReportViewModel.TransactionState.Success -> {
                                println("WalletReportFragment: Displaying ${state.transactions.size} transactions")
                                val items = state.transactions.map { transaction ->
                                    ReportListItems.ReportTransactionItem(
                                        transaction = transaction,
                                        relativeDate = formatDate(transaction.date)
                                    )
                                }
                                transactionsAdapter.submitList(items)
                                updateTotalBalance(state.transactions)
                            }
                            is WalletReportViewModel.TransactionState.Error -> {
                                println("WalletReportFragment: Error: ${state.message}")
                                // Show error to user
                            }
                            is WalletReportViewModel.TransactionState.Loading -> {
                                println("WalletReportFragment: Loading...")
                            }
                            is WalletReportViewModel.TransactionState.Empty -> {
                                println("WalletReportFragment: No transactions found")
                                transactionsAdapter.submitList(emptyList())
                            }
                        }
                    }
                }
            }
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