package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import com.synaptix.budgetbuddy.presentation.ui.main.home.HomeAdapter
import com.synaptix.budgetbuddy.presentation.ui.main.home.HomeMainViewModel

class HomeMainFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeMainViewModel by viewModels()
    private lateinit var homeAdapter: HomeAdapter

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

        // Load wallets from the ViewModel
        viewModel.loadWallets()

        // Observe the wallets data (LiveData) from ViewModel
        viewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            val homeWalletItems = walletList.map { wallet ->
                BudgetReportListItems.HomeWalletItem(
                    walletName = wallet.walletName,
                    walletIcon = R.drawable.baseline_shopping_bag_24, // Sample icon
                    walletBalance = wallet.walletBalance,
                    relativeDate = "Recent" // You can adjust the relativeDate based on your logic
                )
            }
            setupWalletRecycler(homeWalletItems) // Call method with correctly mapped items
        }

        // Set up navigation actions
        binding.txtWallet.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_walletMainFragment)
        }

        binding.txtShowMoreTransactions.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_generalTransactionsFragment)
        }

        binding.txtSeeMonthlyReportOverView.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_generalReportsFragment)
        }
    }

    private fun setupWalletRecycler(walletList: List<BudgetReportListItems.HomeWalletItem>) {
        homeAdapter = HomeAdapter(walletList) // Set the adapter with HomeWalletItem list
        binding.recyclerViewHomeWalletOverview.adapter = homeAdapter // Set the adapter to the RecyclerView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

