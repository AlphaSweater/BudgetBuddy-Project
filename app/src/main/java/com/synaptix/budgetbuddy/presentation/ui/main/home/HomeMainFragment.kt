package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeMainFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeMainViewModel by viewModels()
    private lateinit var homeAdapter: HomeAdapter

    private val TAG = "HomeMainFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflating layout")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view created")

        viewModel.loadWallets()
        Log.d(TAG, "onViewCreated: Requested to load wallets")

        viewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            Log.d(TAG, "wallets.observe: Received ${walletList.size} wallets")

            val homeWalletItems = walletList.map { wallet ->
                Log.d(TAG, "Mapping wallet: ${wallet.walletName}, Balance: ${wallet.walletBalance}")
                BudgetReportListItems.HomeWalletItem(
                    walletName = wallet.walletName,
                    walletIcon = R.drawable.baseline_shopping_bag_24,
                    walletBalance = wallet.walletBalance,
                    relativeDate = "Recent"
                )
            }

            setupWalletRecycler(homeWalletItems)
        }

        binding.txtWallet.setOnClickListener {
            Log.d(TAG, "Navigating to WalletMainFragment")
            findNavController().navigate(R.id.action_homeFragment_to_walletMainFragment)
        }

        binding.txtShowMoreTransactions.setOnClickListener {
            Log.d(TAG, "Navigating to GeneralTransactionsFragment")
            findNavController().navigate(R.id.action_homeFragment_to_generalTransactionsFragment)
        }

        binding.txtSeeMonthlyReportOverView.setOnClickListener {
            Log.d(TAG, "Navigating to GeneralReportsFragment")
            findNavController().navigate(R.id.action_homeFragment_to_generalReportsFragment)
        }
    }

    private fun setupWalletRecycler(walletList: List<BudgetReportListItems.HomeWalletItem>) {
        Log.d(TAG, "Setting up RecyclerView with ${walletList.size} items")
        binding.recyclerViewHomeWalletOverview.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter(walletList)
        binding.recyclerViewHomeWalletOverview.adapter = homeAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up binding")
        _binding = null
    }
}
