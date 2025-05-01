package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetHomePage

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentBudgetHomeBinding
import com.synaptix.budgetbuddy.databinding.FragmentBudgetReportBinding

class BudgetHomeFragment : Fragment() {

    private var _binding: FragmentBudgetHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetHomeViewModel by viewModels()

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
                               savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    private fun setupOnClickListeners() {
        binding.btnBudgetReportItem.setOnClickListener {
            showBudgetReport()
        }
    }

    private fun showBudgetReport() {
        findNavController().navigate(R.id.action_budgetMainFragment_to_budgetReportFragment)
    }
}