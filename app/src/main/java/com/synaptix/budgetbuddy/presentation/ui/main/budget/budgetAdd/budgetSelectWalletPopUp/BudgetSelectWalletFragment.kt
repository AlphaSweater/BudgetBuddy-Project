package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectWalletPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentBudgetSelectWalletBinding
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectWalletBinding
import com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.BudgetAddViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetSelectWalletFragment : Fragment() {

    private var _binding: FragmentBudgetSelectWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetAddViewModel by activityViewModels()
    private val walletViewModel: BudgetSelectWalletViewModel by viewModels()
    private lateinit var budgetSelectWalletAdapter: BudgetSelectWalletAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetSelectWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupOnClickListeners()

        // Observe and update list
        walletViewModel.loadWallets()
        walletViewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            budgetSelectWalletAdapter = BudgetSelectWalletAdapter(walletList) { wallet ->
                viewModel.wallet.value = wallet
                findNavController().popBackStack()
            }
            binding.walletRecyclerView.adapter = budgetSelectWalletAdapter
        }
    }

    private fun setupRecyclerView() {
        binding.walletRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupOnClickListeners() {
        // Add this if you want a "back" button like in the Category fragment
        binding.btnGoBack?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}