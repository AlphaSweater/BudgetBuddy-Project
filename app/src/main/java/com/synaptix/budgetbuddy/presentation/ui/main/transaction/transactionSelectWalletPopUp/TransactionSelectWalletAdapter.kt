package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying a list of wallets in a RecyclerView.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses a dedicated ViewHolder class
 * 3. Handles item click events through a callback
 * 4. Formats currency values consistently
 *
 * @param onWalletClick Callback function that is triggered when a wallet item is clicked
 * @param onEditClick Callback function that is triggered when the edit menu item is clicked
 */
class TransactionSelectWalletAdapter(
    private val onWalletClick: (Wallet) -> Unit,
    private val onEditClick: (Wallet) -> Unit
) : BaseAdapter<Wallet, TransactionSelectWalletAdapter.WalletViewHolder>() {

    /**
     * Creates a new ViewHolder instance for wallet items.
     * Uses the standard item_wallet layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_transaction_wallet
        ) { WalletViewHolder(it) }
    }

    /**
     * ViewHolder class for wallet items.
     * Responsible for binding wallet data to the view and handling click events.
     */
    inner class WalletViewHolder(itemView: View) : BaseViewHolder<Wallet>(itemView) {
        private val name: TextView = itemView.findViewById(R.id.txtWalletName)
        private val balance: TextView = itemView.findViewById(R.id.txtWalletBalance)
        private val menuButton: ImageButton = itemView.findViewById(R.id.btnMenu)

        /**
         * Binds wallet data to the view.
         * Formats the balance with the R currency symbol and 2 decimal places.
         */
        override fun bind(item: Wallet) {
            name.text = item.name
            balance.text = "R %.2f".format(item.balance)

            itemView.setOnClickListener {
                onWalletClick(item)
            }

            menuButton.setOnClickListener { view ->
                showMenu(view, item)
            }
        }

        private fun showMenu(view: View, wallet: Wallet) {
            val popup = android.widget.PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_wallet_item, popup.menu)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(wallet)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }
}
