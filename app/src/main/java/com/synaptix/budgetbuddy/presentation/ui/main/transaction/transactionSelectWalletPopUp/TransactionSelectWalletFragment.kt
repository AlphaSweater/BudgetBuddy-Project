package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectWalletBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionSelectWalletFragment : Fragment() {

    private var _binding: FragmentTransactionSelectWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionAddViewModel by activityViewModels()
    private val walletViewModel: TransactionSelectWalletViewModel by viewModels()

    private val walletAdapter by lazy {
        TransactionSelectWalletAdapter { wallet ->
            viewModel.setWallet(wallet)
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeWallets()
    }

    private fun setupRecyclerView() {
        binding.walletRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = walletAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnGoBack?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeWallets() {
        walletViewModel.loadWallets()
        walletViewModel.wallets.observe(viewLifecycleOwner) { wallets ->
            walletAdapter.submitList(wallets)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}