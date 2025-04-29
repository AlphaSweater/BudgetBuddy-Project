package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class AddNewCategoryFragment : Fragment() {

    companion object {
        fun newInstance() = AddNewCategoryFragment()
    }

    private val viewModel: AddNewCategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_new_category, container, false)
    }
}