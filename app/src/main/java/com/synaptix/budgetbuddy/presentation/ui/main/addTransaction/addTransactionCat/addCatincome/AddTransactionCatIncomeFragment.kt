package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat.addCatincome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat.addCatincome.AddTransactionCatIncomeViewModel

class AddTransactionCatIncomeFragment : Fragment() {

    companion object {
        fun newInstance() = AddTransactionCatIncomeFragment()
    }

    private val viewModel: AddTransactionCatIncomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_transaction_cat_income, container, false)
    }
}