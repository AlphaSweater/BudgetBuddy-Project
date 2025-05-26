package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletReport

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.databinding.FragmentWalletReportBinding

//class WalletReportFragment : Fragment() {
//
//    private var _binding: FragmentWalletReportBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: WalletReportViewModel by viewModels()
//    private lateinit var walletReportAdapter:WalletReportAdapter
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentWalletReportBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupRecyclers()
//        binding.btnGoBack.setOnClickListener { findNavController().popBackStack() }
//        }
//
//    private fun setupRecyclers() {
//        generalTransactionsRecycler()
//    }
//    private fun generalTransactionsRecycler() {
//        // Code for the transaction recycler
//        val items = listOf(
//            BudgetListItems.BudgetDateHeader("1", "Monday", "May 2025", -2000.00),
//            BudgetListItems.BudgetTransactionItem("Lunchhh", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
//            BudgetListItems.BudgetTransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
//            BudgetListItems.BudgetDateHeader("1", "Monday", "April 2025", -4000.00),
//            BudgetListItems.BudgetTransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
//            BudgetListItems.BudgetTransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
//        )
//
//        walletReportAdapter = WalletReportAdapter(items)
//
//        binding.recyclerViewWalletReport.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = walletReportAdapter
//        }
//
//        binding.btnGoBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}