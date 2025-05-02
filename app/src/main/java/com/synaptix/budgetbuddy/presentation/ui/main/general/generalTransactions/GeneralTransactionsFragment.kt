package com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.databinding.FragmentGeneralTransactionsBinding

class GeneralTransactionsFragment : Fragment() {

    private var _binding: FragmentGeneralTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralTransactionsViewModel by viewModels()
    private lateinit var generalTransactionsAdapter: GeneralTransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()

        binding.btnGoBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupRecyclers() {
        generalTransactionsRecycler()
    }
    private fun generalTransactionsRecycler() {
        // Code for the transaction recycler
        val items = listOf(
            BudgetReportListItems.DateHeader("1", "Monday", "May 2025", -2000.00),
            BudgetReportListItems.TransactionItem("Lunchhh", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
            BudgetReportListItems.TransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
            BudgetReportListItems.DateHeader("1", "Monday", "April 2025", -4000.00),
            BudgetReportListItems.TransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
            BudgetReportListItems.TransactionItem("Lunch", R.drawable.ic_car_24, R.color.cat_orange, 200.00, "Cash", null, "Today"),
        )

        generalTransactionsAdapter = GeneralTransactionsAdapter(items)

        binding.recyclerViewGeneralTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = generalTransactionsAdapter
        }

        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}