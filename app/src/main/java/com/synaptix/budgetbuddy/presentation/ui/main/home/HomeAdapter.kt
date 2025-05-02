package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.HomeWalletItem
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.TransactionItem
import com.synaptix.budgetbuddy.core.model.Wallet

class HomeAdapter(private val items: List<BudgetReportListItems>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_WALLET = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeWalletItem -> VIEW_TYPE_WALLET
            is TransactionItem -> VIEW_TYPE_TRANSACTION
            else -> throw IllegalArgumentException("Invalid item type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_wallet, parent, false)
        return when (viewType) {
            VIEW_TYPE_WALLET -> WalletViewHolder(view)
            VIEW_TYPE_TRANSACTION -> TransactionViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeWalletItem -> (holder as WalletViewHolder).bind(item)
            is TransactionItem -> (holder as TransactionViewHolder).bind(item)
            else -> throw IllegalArgumentException("Unexpected item type at $position")
        }
    }

    override fun getItemCount(): Int = items.size

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: HomeWalletItem) {
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
            val nameText = itemView.findViewById<TextView>(R.id.txtCategoryName)
            val balanceText = itemView.findViewById<TextView>(R.id.txtAmount)
            val dateText = itemView.findViewById<TextView>(R.id.txtDate)
            val transactionsText = itemView.findViewById<TextView>(R.id.txtTransactions)

            iconView.setImageResource(item.walletIcon)
            nameText.text = item.walletName
            balanceText.text = "R${item.walletBalance}"
            dateText.text = item.relativeDate
            transactionsText.text = "Wallet"
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TransactionItem) {
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
            val iconContainer = itemView.findViewById<LinearLayout>(R.id.iconCategoryContainer)

            val nameText = itemView.findViewById<TextView>(R.id.txtCategoryName)
            val amountText = itemView.findViewById<TextView>(R.id.txtAmount)
            val dateText = itemView.findViewById<TextView>(R.id.txtDate)
            val walletText = itemView.findViewById<TextView>(R.id.txtTransactions)

            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)

            iconView.setImageResource(item.categoryIcon)
            nameText.text = item.categoryName
            amountText.text = "R${item.amount}"
            dateText.text = item.relativeDate
            walletText.text = item.walletName
        }
    }
}
