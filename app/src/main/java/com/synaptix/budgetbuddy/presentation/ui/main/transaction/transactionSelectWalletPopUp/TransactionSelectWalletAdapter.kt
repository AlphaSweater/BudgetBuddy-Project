package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

class TransactionSelectWalletAdapter(
    private val onWalletClick: (Wallet) -> Unit
) : BaseAdapter<Wallet, TransactionSelectWalletAdapter.WalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_wallet
        ) { WalletViewHolder(it) }
    }

    inner class WalletViewHolder(itemView: View) : BaseViewHolder<Wallet>(itemView) {
        private val name: TextView = itemView.findViewById(R.id.walletName)
        private val balance: TextView = itemView.findViewById(R.id.walletBalance)

        override fun bind(item: Wallet) {
            name.text = item.walletName
            balance.text = "R %.2f".format(item.walletBalance)

            itemView.setOnClickListener {
                onWalletClick(item)
            }
        }
    }
}
