package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.synaptix.budgetbuddy.databinding.FragmentWalletMainBinding
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Wallet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletMainFragment : Fragment() {

    private var _binding: FragmentWalletMainBinding? = null
    private val binding get() = _binding!!

    private val walletViewModel: WalletMainViewModel by activityViewModels()
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
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.apply {
            // Setup RecyclerView
            recyclerViewWalletMain.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = walletAdapter
            }

            // Setup click listeners
            btnCreateWallet.setOnClickListener {
                findNavController().navigate(R.id.action_walletMainFragment_to_addWalletFragment)
            }

            ivEye.setOnClickListener {
                walletViewModel.toggleBalanceVisibility()
            }
        }
    }

    private fun setupPieChart(wallets: List<Wallet>) {
        val pieChart: PieChart = binding.pieChart
        val context = pieChart.context

        // Convert wallet balances into PieEntries
        val entries = wallets.map { PieEntry(it.balance.toFloat(), it.name) }

        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = ContextCompat.getColor(context, R.color.light_text)
            valueTextSize = 14f
        }

        val data = PieData(dataSet)

        // Format values to show percentages
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.apply {
            this.data = data
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 50f
            setUsePercentValues(true)
            setDrawEntryLabels(true) // Shows labels like wallet name
            setEntryLabelColor(ContextCompat.getColor(context, R.color.light_text))
            setEntryLabelTextSize(12f)
            description.isEnabled = false // Hides the description label
            legend.isEnabled = false      // Hides the legend at the bottom
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }




    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect wallet state
                launch {
                    walletViewModel.walletState.collectLatest { state ->
                        handleWalletState(state)
                    }
                }

                // Collect balance visibility
                launch {
                    walletViewModel.isBalanceVisible.collectLatest { isVisible ->
                        updateBalanceVisibility(isVisible)
                    }
                }

                // Collect total balance
                launch {
                    walletViewModel.totalBalance.collectLatest { total ->
                        if (walletViewModel.isBalanceVisible.value) {
                            binding.tvCurrencyTotal.text = total.toString()
                        }
                    }
                }
            }
        }
    }

    private fun handleWalletState(state: WalletMainViewModel.WalletState) {
        binding.apply {
            when (state) {
                is WalletMainViewModel.WalletState.Loading -> {
                    showLoadingState(
                        recyclerView = recyclerViewWalletMain,
                        progressBar = progressBarWallets,
                        emptyText = txtEmptyWallets
                    )
                }
                is WalletMainViewModel.WalletState.Success -> {
                    hideLoadingState(progressBarWallets)
                    showContentState(
                        recyclerView = recyclerViewWalletMain,
                        emptyText = txtEmptyWallets
                    )
                    updateWalletsList(state.wallets)
                }
                is WalletMainViewModel.WalletState.Empty -> {
                    hideLoadingState(progressBarWallets)
                    showEmptyState(
                        recyclerView = recyclerViewWalletMain,
                        emptyText = txtEmptyWallets,
                        message = getString(R.string.no_wallets_found)
                    )
                }
                is WalletMainViewModel.WalletState.Error -> {
                    hideLoadingState(progressBarWallets)
                    showEmptyState(
                        recyclerView = recyclerViewWalletMain,
                        emptyText = txtEmptyWallets,
                        message = getString(R.string.no_wallets_found)
                    )
                }
            }
        }
    }

    private fun updateWalletsList(wallets: List<Wallet>) {
        val budgetWalletItems = wallets.map { wallet ->
            BudgetListItems.BudgetWalletItem(
                wallet = wallet,
                walletName = wallet.name,
                walletIcon = R.drawable.ic_ui_wallet,
                walletBalance = wallet.balance,
                relativeDate = wallet.formatDate(wallet.lastTransactionAt)
            )
        }
        walletAdapter.submitList(budgetWalletItems)
    }

    private fun updateBalanceVisibility(isVisible: Boolean) {
        binding.tvCurrencyTotal.text = if (isVisible) {
            walletViewModel.totalBalance.value.toString()
        } else {
            "****"
        }
    }

    // Helper functions for UI state management
    private fun showLoadingState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        progressBar: View,
        emptyText: TextView
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = false
        progressBar.isVisible = true
    }

    private fun hideLoadingState(progressBar: View) {
        progressBar.isVisible = false
    }

    private fun showEmptyState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        emptyText: TextView,
        message: String
    ) {
        recyclerView.isVisible = false
        emptyText.isVisible = true
        emptyText.text = message
    }

    private fun showContentState(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        emptyText: TextView
    ) {
        recyclerView.isVisible = true
        emptyText.isVisible = false
    }

    private fun onWalletClicked(wallet: BudgetListItems.BudgetWalletItem) {
        // TODO: Navigate to wallet details
        // findNavController().navigate(R.id.action_walletMainFragment_to_walletDetailsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}