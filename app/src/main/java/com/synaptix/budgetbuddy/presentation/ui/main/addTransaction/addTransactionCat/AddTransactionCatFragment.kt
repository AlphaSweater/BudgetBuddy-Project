package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.synaptix.budgetbuddy.R

class AddTransactionCatFragment : Fragment() {

    companion object {
        fun newInstance() = AddTransactionCatFragment()
    }

    private val viewModel: AddTransactionCatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You can use viewModel here for data-related logic
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_transaction_cat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
