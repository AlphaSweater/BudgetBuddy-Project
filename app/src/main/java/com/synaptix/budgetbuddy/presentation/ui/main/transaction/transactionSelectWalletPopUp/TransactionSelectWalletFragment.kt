package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectWalletBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class TransactionSelectWalletFragment : Fragment() {

    private var _binding: FragmentTransactionSelectWalletBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: TransactionAddViewModel by navGraphViewModels(R.id.ind_transaction_navigation_graph) {defaultViewModelProviderFactory}
    private val walletViewModel: TransactionSelectWalletViewModel by viewModels()

    private val walletAdapter by lazy {
        TransactionSelectWalletAdapter(
            onWalletClick = { wallet ->
                sharedViewModel.setWallet(wallet)
                findNavController().popBackStack()
            },
            onEditClick = { wallet ->
                navigateToEditWallet(wallet)
            }
        )
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
        setupSearch()
        observeViewModel()
        walletViewModel.loadWallets()
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

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            walletViewModel.filterWallets(text?.toString() ?: "")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    walletViewModel.uiState.collect { state ->
                        handleUiState(state)
                    }
                }

                // Collect filtered wallets
                launch {
                    walletViewModel.filteredWallets.collect { wallets ->
                        walletAdapter.submitList(wallets)
                        updateEmptyState(wallets.isEmpty())
                    }
                }
            }
        }
    }

    private fun handleUiState(state: TransactionSelectWalletViewModel.UiState) {
        when (state) {
            is TransactionSelectWalletViewModel.UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
            }
            is TransactionSelectWalletViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
            is TransactionSelectWalletViewModel.UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
                showError(state.message)
            }
            else -> {
                binding.progressBar.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }

    private fun navigateToEditWallet(wallet: Wallet) {
        // Create a bundle with the necessary arguments
        val bundle = bundleOf(
            "screenMode" to "EDIT",
            "walletId" to wallet.id
        )

        findNavController().navigate(
            R.id.ind_wallet_navigation_graph,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}