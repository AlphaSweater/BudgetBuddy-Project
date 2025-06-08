package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletReport

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports.ReportListItems

// WalletReportAdapter.kt
class WalletReportAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<ReportListItems.ReportTransactionItem, WalletReportAdapter.TransactionViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_wallet, parent, false)
        return TransactionViewHolder(view, onTransactionClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        itemView: View,
        private val onTransactionClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val icon = itemView.findViewById<ImageView>(R.id.imgWalletIcon)
        private val tvTitle = itemView.findViewById<TextView>(R.id.txtWalletName)
        private val tvDate = itemView.findViewById<TextView>(R.id.txtRelativeDay)
        private val tvAmount = itemView.findViewById<TextView>(R.id.txtWalletBalance)

        fun bind(item: ReportListItems.ReportTransactionItem) {
            val transaction = item.transaction
            icon.setImageResource(transaction.category.icon)
            icon.setColorFilter(ContextCompat.getColor(itemView.context, transaction.category.color))
            tvTitle.text = transaction.category.name
            tvDate.text = item.relativeDate
            tvAmount.text = String.format("R %.2f", transaction.amount)

            // Set amount color based on transaction type
            val amountColor = if (transaction.amount >= 0) {
                ContextCompat.getColor(itemView.context, R.color.profit_green)
            } else {
                ContextCompat.getColor(itemView.context, R.color.expense_red)
            }
            tvAmount.setTextColor(amountColor)

            itemView.setOnClickListener { onTransactionClick(transaction) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportListItems.ReportTransactionItem>() {
        override fun areItemsTheSame(
            oldItem: ReportListItems.ReportTransactionItem,
            newItem: ReportListItems.ReportTransactionItem
        ): Boolean {
            return oldItem.transaction.id == newItem.transaction.id
        }

        override fun areContentsTheSame(
            oldItem: ReportListItems.ReportTransactionItem,
            newItem: ReportListItems.ReportTransactionItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}