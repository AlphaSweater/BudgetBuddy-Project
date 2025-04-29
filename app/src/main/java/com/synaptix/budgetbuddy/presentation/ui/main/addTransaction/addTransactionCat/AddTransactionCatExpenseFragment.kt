package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class AddTransactionCatExpenseFragment : Fragment() {

    companion object {
        fun newInstance() = AddTransactionCatExpenseFragment()
    }

    private val viewModel: AddTransactionCatExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can use viewModel here for data-related logic
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_transaction_cat_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
