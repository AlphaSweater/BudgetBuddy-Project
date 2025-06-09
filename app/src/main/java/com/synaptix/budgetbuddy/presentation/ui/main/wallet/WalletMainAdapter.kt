package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.core.util.DateUtil
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying the main wallet list in the wallet screen.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses a dedicated ViewHolder class
 * 3. Handles item click events through a callback
 * 4. Displays wallet icon, name, and balance
 *
 * @param onWalletClick Callback function that is triggered when a wallet item is clicked
 */
class WalletMainAdapter(
    private val onWalletClick: (BudgetListItems.BudgetWalletItem) -> Unit
) : BaseAdapter<BudgetListItems.BudgetWalletItem, WalletMainAdapter.WalletViewHolder>() {

    /**
     * Creates a new ViewHolder instance for wallet items.
     * Uses the standard item_wallet_main layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        return createViewHolder(parent, R.layout.item_wallet_main) { view ->
            WalletViewHolder(view, onWalletClick)
        }
    }

    /**
     * ViewHolder class for wallet items in the main wallet screen.
     * Responsible for binding wallet data to the view and handling click events.
     * Displays:
     * - Wallet icon
     * - Wallet name
     * - Wallet balance
     */
    class WalletViewHolder(
        itemView: View,
        private val onWalletClick: (BudgetListItems.BudgetWalletItem) -> Unit
    ) : BaseViewHolder<BudgetListItems.BudgetWalletItem>(itemView) {

        private val walletIcon: ImageView = itemView.findViewById(R.id.walletIcon)
        private val walletName: TextView = itemView.findViewById(R.id.walletName)
        private val walletBalance: TextView = itemView.findViewById(R.id.walletBalance)
        private val dateText: TextView = itemView.findViewById(R.id.lastActivity)
        private val context = itemView.context

        /**
         * Binds wallet data to the view.
         * Sets the wallet icon, name, and balance.
         * Attaches click listener to the entire item view.
         */
        override fun bind(item: BudgetListItems.BudgetWalletItem) {
            walletIcon.setImageResource(item.walletIcon)
            walletName.text = item.walletName
            walletBalance.text = CurrencyUtil.formatWithSymbol(item.walletBalance)
            dateText.text = "• ${DateUtil.formatDate(item.wallet.lastTransactionAt)}"

            // Set text color based on balance
            val colorRes = if (item.walletBalance < 0) {
                R.color.error // Red color for negative balance
            } else {
                R.color.profit_green // Default text color
            }
            walletBalance.setTextColor(ContextCompat.getColor(context, colorRes))

            dateText.text = "• ${DateUtil.formatDate(item.wallet.lastTransactionAt)}"

            itemView.setOnClickListener { onWalletClick(item) }
        }
        }
    }