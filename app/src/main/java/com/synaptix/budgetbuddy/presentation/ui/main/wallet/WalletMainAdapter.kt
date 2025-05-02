package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.core.model.Wallet

class WalletMainAdapter(private val walletItems: List<BudgetReportListItems.WalletItem>,
                        private val onClick: (BudgetReportListItems.WalletItem) -> Unit // <-- Add this
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BUDGET = 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (walletItems[position]) {
            is BudgetReportListItems.WalletItem -> VIEW_TYPE_BUDGET
            else -> throw IllegalArgumentException("Unsupported item type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BUDGET -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_wallet_main, parent, false) // Make sure this is not your fragment layout!
                WalletViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int = walletItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val walletItems = walletItems[position]) {
            is BudgetReportListItems.WalletItem -> (holder as WalletViewHolder).bind(walletItems, onClick)
            else -> throw IllegalArgumentException("Unsupported item type at position $position")
        }
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val walletIcon: ImageView = itemView.findViewById(R.id.walletIcon)
        private val walletName: TextView = itemView.findViewById(R.id.walletName)
        private val walletBalance: TextView = itemView.findViewById(R.id.walletBalance)

        fun bind(item: BudgetReportListItems.WalletItem, onClick: (BudgetReportListItems.WalletItem) -> Unit) {
            walletIcon.setImageResource(item.walletIcon)
            walletName.text = item.walletName
            walletBalance.text = item.walletBalance.toString()
            itemView.setOnClickListener { onClick(item) }
        }
    }
}