package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentWalletMainBinding
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.core.model.Wallet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletMainFragment : Fragment() {

    private var _binding: FragmentWalletMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletMainViewModel by viewModels()
    private val walletAdapter by lazy {
        WalletMainAdapter { walletItem ->
            onWalletClicked(walletItem)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.fetchWallets()
    }

    private fun setupUI() {
        setupRecyclerView()
        setupClickListeners()
        setupBalanceVisibility()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewWalletMain.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = walletAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnCreateWallet.setOnClickListener {
            findNavController().navigate(R.id.action_walletMainFragment_to_addWalletFragment)
        }

        binding.ivEye.setOnClickListener {
            viewModel.toggleBalanceVisibility()
        }
    }

    private fun setupBalanceVisibility() {
        viewModel.isBalanceVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.tvCurrencyTotal.text = if (isVisible) {
                viewModel.totalBalance.value?.toString() ?: "0"
            } else {
                "****"
            }
        }
    }

    private fun observeViewModel() {
        viewModel.wallets.observe(viewLifecycleOwner) { wallets ->
            updateWalletsList(wallets)
        }

        viewModel.totalBalance.observe(viewLifecycleOwner) { total ->
            if (viewModel.isBalanceVisible.value == true) {
                binding.tvCurrencyTotal.text = total.toString()
            }
        }
    }

    private fun updateWalletsList(wallets: List<Wallet>) {
        val walletItems = wallets.map { wallet ->
            BudgetReportListItems.WalletItem(
                walletName = wallet.walletName,
                walletBalance = wallet.walletBalance,
                walletIcon = R.drawable.baseline_shopping_bag_24
            )
        }

        binding.txtEmptyWallets.isVisible = walletItems.isEmpty()
        binding.recyclerViewWalletMain.isVisible = walletItems.isNotEmpty()
        
        walletAdapter.submitList(walletItems, enableDiffUtil = true)
    }

    private fun onWalletClicked(wallet: BudgetReportListItems.WalletItem) {
        // TODO: Navigate to wallet details
        // findNavController().navigate(R.id.action_walletMainFragment_to_walletDetailsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}