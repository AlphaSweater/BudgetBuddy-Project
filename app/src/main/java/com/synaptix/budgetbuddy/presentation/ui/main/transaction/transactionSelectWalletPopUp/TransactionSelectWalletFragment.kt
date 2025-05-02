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
    private lateinit var transactionSelectWalletAdapter: TransactionSelectWalletAdapter

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
        setupOnClickListeners()

        walletViewModel.loadWallets()
        walletViewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            transactionSelectWalletAdapter = TransactionSelectWalletAdapter(walletList) { wallet ->
                viewModel.wallet.value = wallet
                findNavController().popBackStack()
            }
            binding.walletRecyclerView.adapter = transactionSelectWalletAdapter
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