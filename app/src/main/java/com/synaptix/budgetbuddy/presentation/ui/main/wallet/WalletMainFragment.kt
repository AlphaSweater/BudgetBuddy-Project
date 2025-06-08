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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.core.util.DateUtil
import com.synaptix.budgetbuddy.databinding.FragmentWalletMainBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

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

    private val walletId: String? by lazy {
        arguments?.getString("walletId")
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

        lineChart.clear()
        lineChart.setNoDataText("No transaction data available")
        lineChart.setNoDataTextColor(context.getThemeColor(R.attr.bb_primaryText))

        if (transactions.isEmpty()) {
            lineChart.visibility = View.GONE
            return
        }

        lineChart.visibility = View.VISIBLE

        // Sort transactions by date (oldest first)
        val sortedTransactions = transactions.sortedBy { it.date }

        // Track transaction types
        var hasIncome = false
        var hasExpenses = false

        // Get unique transaction dates
        val transactionDates = sortedTransactions.map {
            // Check transaction type
            if (it.category.type.equals("income", true)) {
                hasIncome = true
            } else {
                hasExpenses = true
            }

            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sorted()

        if (transactionDates.isEmpty()) {
            lineChart.visibility = View.GONE
            return
        }

        // Group transactions by day
        val dailyTransactions = sortedTransactions.groupBy { transaction ->
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        // Prepare data entries and x-axis labels
        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()
        var runningBalance = 0.0

        // Add initial point at 0
        entries.add(Entry(0f, runningBalance.toFloat()))
        xLabels.add("")

        // Calculate running balance for each transaction date
        transactionDates.forEachIndexed { index, date ->
            val dailyNet = dailyTransactions[date]?.sumOf {
                if (it.category.type.equals("income", true)) {
                    it.amount.toDouble()  // Income adds to balance
                } else {
                    -it.amount.toDouble() // Expenses subtract from balance
                }
            } ?: 0.0

            runningBalance += dailyNet
            entries.add(Entry((index + 1).toFloat(), runningBalance.toFloat()))
            xLabels.add(DateUtil.formatDate(date, true))
        }

        // Determine line and gradient colors based on transaction types
        val (lineColorResId, gradientResId) = when {
            hasIncome && hasExpenses -> Pair(R.color.button, R.drawable.gradient_wallet)
            hasIncome -> Pair(R.color.profit_green, R.drawable.gradient_income)
            else -> Pair(R.color.expense_red, R.drawable.gradient_expense)
        }

        val lineColor = ContextCompat.getColor(context, lineColorResId)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.bb_primaryText, typedValue, true)
        val primaryTextColor = ContextCompat.getColor(context, typedValue.resourceId)

        // Create dataset
        val dataSet = LineDataSet(entries, "Balance").apply {
            color = lineColor
            lineWidth = 2.5f
            setCircleColor(lineColor)
            circleRadius = 3f
            setDrawCircleHole(false)
            mode = LineDataSet.Mode.LINEAR
            setDrawValues(false)
            setDrawCircles(entries.size <= 15)
            circleHoleRadius = 2f
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(false)

            // Set gradient fill
            fillDrawable = ContextCompat.getDrawable(context, gradientResId)
            setDrawFilled(true)
            fillAlpha = 80
        }

        val lineData = LineData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value >= 0) {
                        "R${"%,.0f".format(value.toDouble())}"
                    } else {
                        "-R${"%,.0f".format(abs(value.toDouble()))}"
                    }
                }
            })
        }

        lineChart.apply {
            data = lineData

            // Configure x-axis
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(xLabels)
                granularity = 1f
                textColor = primaryTextColor
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                setAvoidFirstLastClipping(true)
                setCenterAxisLabels(false)
                axisMinimum = 0f
                axisMaximum = (xLabels.size - 1).toFloat()
                setLabelCount(minOf(xLabels.size, 10), true)
            }

            // Configure y-axis
            axisLeft.apply {
                textColor = primaryTextColor
                setDrawGridLines(true)
                gridColor = Color.argb(50,
                    Color.red(primaryTextColor),
                    Color.green(primaryTextColor),
                    Color.blue(primaryTextColor)
                )
                gridLineWidth = 0.5f
                axisLineColor = Color.argb(50,
                    Color.red(primaryTextColor),
                    Color.green(primaryTextColor),
                    Color.blue(primaryTextColor)
                )
                axisLineWidth = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 0) {
                            "R${"%,.0f".format(value.toDouble())}"
                        } else {
                            "-R${"%,.0f".format(abs(value.toDouble()))}"
                        }
                    }
                }

                // Calculate min and max values with some padding
                val minY = entries.minByOrNull { it.y }?.y ?: 0f
                val maxY = entries.maxByOrNull { it.y }?.y ?: 0f
                val range = maxY - minY
                val padding = if (range > 0) range * 0.1f else 10f

                // Always include 0 in the y-axis range
                axisMinimum = minOf(minY - padding, 0f)
                axisMaximum = maxY + padding

                // Add zero line
                setDrawZeroLine(true)
                zeroLineColor = Color.argb(100,
                    Color.red(primaryTextColor),
                    Color.green(primaryTextColor),
                    Color.blue(primaryTextColor)
                )
                zeroLineWidth = 1f
            }

            // Configure chart appearance
            setExtraOffsets(24f, 24f, 24f, 40f)
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false

            // Configure viewport
            setScaleEnabled(true)
            isDragEnabled = true
            setPinchZoom(true)

            // Set viewport to show all data
            setVisibleXRange(0f, minOf(7f, transactionDates.size.toFloat()))
            moveViewToX(0f)

            // Add nice animation
            animateX(1200, Easing.EaseInOutQuad)
            animateY(1200, Easing.EaseInOutQuad)

            // Refresh
            invalidate()
        }
    }

    private fun generateDateRange(startDate: Long, endDate: Long): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = endDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        while (calendar.timeInMillis <= endCal.timeInMillis) {
            dates.add(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
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
                            binding.tvCurrencyTotal.text = CurrencyUtil.formatWithoutSymbol(total)
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
                relativeDate = DateUtil.formatDate(wallet.lastTransactionAt)
            )
        }
        walletAdapter.submitList(budgetWalletItems)
    }

    private fun updateBalanceVisibility(isVisible: Boolean) {
        binding.tvCurrencyTotal.text = if (isVisible) {
            CurrencyUtil.formatWithSymbol(walletViewModel.totalBalance.value)
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
        println("WalletMainFragment: Navigating to wallet report for wallet ID: ${wallet.wallet.id}")
        val bundle = Bundle().apply {
            putString("walletId", wallet.wallet.id)
        }
        findNavController().navigate(R.id.action_walletMainFragment_to_walletReportFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}