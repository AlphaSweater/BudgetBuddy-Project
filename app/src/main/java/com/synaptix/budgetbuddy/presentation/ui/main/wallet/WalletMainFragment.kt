package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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

    private fun setupPieChart(wallets: List<Wallet>) {
        val pieChart: PieChart = binding.pieChart
        val context = pieChart.context

        // Convert wallet balances into PieEntries
        val entries = wallets.map { PieEntry(it.balance.toFloat(), it.name) }

        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        val data = PieData(dataSet)

        // Format values to show percentages
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.apply {
            this.data = data
            setUsePercentValues(true)
            setDrawEntryLabels(true) // Shows labels like wallet name
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            description.isEnabled = false // Hides the description label
            legend.isEnabled = false      // Hides the legend at the bottom
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }




    private fun observeViewModel() {
        viewModel.wallets.observe(viewLifecycleOwner) { wallets ->
            updateWalletsList(wallets)
            setupPieChart(wallets)
        }

        viewModel.totalBalance.observe(viewLifecycleOwner) { total ->
            if (viewModel.isBalanceVisible.value == true) {
                binding.tvCurrencyTotal.text = total.toString()
            }
        }
    }

    private fun updateWalletsList(wallets: List<Wallet>) {
        val budgetWalletItems = wallets.map { wallet ->
            BudgetListItems.BudgetWalletItem(
                wallet = wallet,
                walletName = wallet.name,
                walletIcon = R.drawable.baseline_shopping_bag_24,
                walletBalance = wallet.balance,
                relativeDate = wallet.formatDate(wallet.lastTransactionAt)
            )
        }

        binding.txtEmptyWallets.isVisible = budgetWalletItems.isEmpty()
        binding.recyclerViewWalletMain.isVisible = budgetWalletItems.isNotEmpty()
        
        walletAdapter.submitList(budgetWalletItems)
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