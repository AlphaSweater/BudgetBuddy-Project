package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectIconPopUp

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class BudgetSelectIconFragment : Fragment() {

    companion object {
        fun newInstance() = BudgetSelectIconFragment()
    }

    private val viewModel: BudgetSelectIconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_budget_select_icon, container, false)
    }
}