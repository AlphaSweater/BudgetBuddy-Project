package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentWalletMainBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private fun setupLineChart(transactions: List<Transaction>) {
        val lineChart = binding.lineChart
        val context = lineChart.context

        if (transactions.isEmpty()) {
            lineChart.visibility = View.GONE
            return
        }

        lineChart.visibility = View.VISIBLE
        lineChart.clear()

        // Get theme colors
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.bb_button, typedValue, true)
        val lineColor = ContextCompat.getColor(context, typedValue.resourceId)
        context.theme.resolveAttribute(R.attr.bb_primaryText, typedValue, true)
        val primaryTextColor = ContextCompat.getColor(context, typedValue.resourceId)

        // Sort transactions by date
        val sortedTransactions = transactions.sortedBy { it.date }

        // Get date range (from first transaction to today)
        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val firstTransactionDate = sortedTransactions.firstOrNull()?.date ?: run {
            lineChart.visibility = View.GONE
            return
        }

        // Group transactions by date
        val transactionsByDate = sortedTransactions
            .groupBy { transaction ->
                calendar.timeInMillis = transaction.date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }

        // Get all transaction dates
        val transactionDates = transactionsByDate.keys.sorted()

        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        var runningBalance = 0.0
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        // Skip the initial zero point and start from first transaction
        var firstTransaction = true

        // Process each transaction date
        transactionDates.forEachIndexed { index, date ->
            // Get transactions for this day
            val dailyTransactions = transactionsByDate[date] ?: return@forEachIndexed
            val dailyNet = dailyTransactions.sumOf { it.amount }

            // Update running balance
            runningBalance += dailyNet

            // Add entry for this day (skip the first point if it's zero)
            if (!firstTransaction || runningBalance != 0.0) {
                entries.add(Entry(index.toFloat(), runningBalance.toFloat()))
                xLabels.add(if (index % 3 == 0) dateFormat.format(Date(date)) else "")
                firstTransaction = false
            }
        }

        // If no valid entries, hide the chart
        if (entries.isEmpty()) {
            lineChart.visibility = View.GONE
            return
        }

        // Create dataset with curved line
        val dataSet = LineDataSet(entries, "Balance").apply {
            color = lineColor
            lineWidth = 2.5f
            setCircleColor(lineColor)
            circleRadius = 4f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_wallet)
            setDrawValues(false)
            setDrawCircles(true)
            circleHoleRadius = 2f
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(false)
            fillAlpha = 80
        }

        val lineData = LineData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R${value.toInt()}"
                }
            })
        }

        lineChart.apply {
            clear()
            data = lineData

            // Configure x-axis (bottom)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = primaryTextColor
                setDrawGridLines(false)
                axisLineColor = Color.argb(50,
                    Color.red(primaryTextColor),
                    Color.green(primaryTextColor),
                    Color.blue(primaryTextColor)
                )
                axisLineWidth = 1f
                labelRotationAngle = -45f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in xLabels.indices) xLabels[index] else ""
                    }
                }
                // Only show some labels to avoid crowding
                setLabelCount(minOf(7, xLabels.size), true)
                setAvoidFirstLastClipping(true)
                // Remove first label
                setAxisMinimum(-0.5f)
            }

            // Configure y-axis (left)
            axisLeft.apply {
                textColor = primaryTextColor
                setDrawGridLines(false)
                axisLineColor = Color.argb(50,
                    Color.red(primaryTextColor),
                    Color.green(primaryTextColor),
                    Color.blue(primaryTextColor)
                )
                axisLineWidth = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R${value.toInt()}"
                    }
                }
                // Calculate nice axis range
                val minY = entries.minOfOrNull { it.y } ?: 0f
                val maxY = entries.maxOfOrNull { it.y } ?: 0f
                val padding = maxOf(10f, (maxY - minY) * 0.1f)
                axisMinimum = (minY - padding).coerceAtLeast(0f)
                axisMaximum = maxY + padding
            }

            // Disable right axis
            axisRight.isEnabled = false

            // Disable legend
            legend.isEnabled = false

            // Disable description
            description.isEnabled = false

            // Enable touch interaction
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDoubleTapToZoomEnabled(true)

            // Set extra offsets for better display
            setExtraOffsets(10f, 20f, 10f, 30f)

            // Set transparent background
            setBackgroundColor(Color.TRANSPARENT)

            // Animation
            animateY(1000, Easing.EaseInOutCubic)

            // Refresh the chart
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
                // Collect transactions
                launch {
                    walletViewModel.transactions.collect { transactions ->
                        // Filter out any invalid transactions
                        val validTransactions = transactions.filter { it.amount != 0.0 }
                        setupLineChart(validTransactions)
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