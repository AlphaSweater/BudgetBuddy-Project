package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

class WalletMainAdapter(
    private val onWalletClick: (BudgetReportListItems.WalletItem) -> Unit
) : BaseAdapter<BudgetReportListItems.WalletItem, WalletMainAdapter.WalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        return createViewHolder(parent, R.layout.item_wallet_main) { view ->
            WalletViewHolder(view, onWalletClick)
        }
    }

    class WalletViewHolder(
        itemView: View,
        private val onWalletClick: (BudgetReportListItems.WalletItem) -> Unit
    ) : BaseViewHolder<BudgetReportListItems.WalletItem>(itemView) {

        private val walletIcon: ImageView = itemView.findViewById(R.id.walletIcon)
        private val walletName: TextView = itemView.findViewById(R.id.walletName)
        private val walletBalance: TextView = itemView.findViewById(R.id.walletBalance)

        override fun bind(item: BudgetReportListItems.WalletItem) {
            walletIcon.setImageResource(item.walletIcon)
            walletName.text = item.walletName
            walletBalance.text = item.walletBalance.toString()
            
            itemView.setOnClickListener { onWalletClick(item) }
        }
    }
}