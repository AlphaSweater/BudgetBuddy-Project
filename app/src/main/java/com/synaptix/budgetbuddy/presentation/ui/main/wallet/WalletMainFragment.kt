package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentWalletMainBinding
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.core.model.Wallet

class WalletMainFragment : Fragment() {

    companion object {
        fun newInstance() = WalletMainFragment()
    }
    private var _binding: FragmentWalletMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
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

        val userId = 1
        viewModel.fetchWallets(userId)

        viewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            setupRecyclerView(walletList)
        }
    }

    private fun setupRecyclerView(walletList: List<Wallet>) {
        val walletItems = walletList.map { wallet ->
            BudgetReportListItems.WalletItem(
                walletName = wallet.walletName,
                walletBalance = wallet.walletBalance,
                walletIcon = R.drawable.baseline_shopping_bag_24 //All wallets have same logo
            )
        }

        val walletMainAdapter = WalletMainAdapter(walletItems) {
            findNavController().navigate(R.id.action_walletMainFragment_to_walletReportFragment)
        }

        binding.recyclerViewWalletMain.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = walletMainAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//private fun recyclerViewWalletMain() {
//    val walletItems = listOf(
//        BudgetReportListItems.WalletItem(
//            walletName = "Groceries",
//            walletBalance = 5000.00,
//            walletIcon = R.drawable.baseline_shopping_bag_24),
//        BudgetReportListItems.WalletItem(
//            walletName = "Groceries",
//            walletBalance = 5000.00,
//            walletIcon = R.drawable.baseline_shopping_bag_24),
//        BudgetReportListItems.WalletItem(
//            walletName = "Groceries",
//            walletBalance = 5000.00,
//            walletIcon = R.drawable.baseline_shopping_bag_24)
//    )
//
//    val walletMainAdapter = WalletMainAdapter(walletItems) { item ->
//        findNavController().navigate(R.id.action_walletMainFragment_to_walletReportFragment)
//    }
//
//    binding.recyclerViewWalletMain.apply {
//        layoutManager = LinearLayoutManager(requireContext())
//        adapter = walletMainAdapter
//    }
//}