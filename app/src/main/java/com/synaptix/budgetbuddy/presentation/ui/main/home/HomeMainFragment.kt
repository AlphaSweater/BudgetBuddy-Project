package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding


class HomeMainFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeMainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtWallet.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_walletMainFragment)
        }

        binding.txtShowMoreTransactions.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_generalTransactionsFragment)
        }

        binding.txtSeeMonthlyReportOverView.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_homeFragment_to_generalReportsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}