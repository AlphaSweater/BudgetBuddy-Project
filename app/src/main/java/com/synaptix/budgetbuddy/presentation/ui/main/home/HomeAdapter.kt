package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.graphics.drawable.GradientDrawable
import android.util.Log
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

class HomeAdapter(private val items: List<BudgetReportListItems>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_WALLET = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val TAG = "HomeAdapter"
    }

    override fun getItemViewType(position: Int): Int {
        val type = when (items[position]) {
            is HomeWalletItem -> VIEW_TYPE_WALLET
            is TransactionItem -> VIEW_TYPE_TRANSACTION
            else -> throw IllegalArgumentException("Invalid item type at position $position")
        }
        Log.d(TAG, "getItemViewType: position=$position, type=$type")
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder: viewType=$viewType")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_wallet, parent, false)
        return when (viewType) {
            VIEW_TYPE_WALLET -> {
                Log.d(TAG, "Creating WalletViewHolder")
                WalletViewHolder(view)
            }
            VIEW_TYPE_TRANSACTION -> {
                Log.d(TAG, "Creating TransactionViewHolder")
                TransactionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeWalletItem -> {
                Log.d(TAG, "Binding HomeWalletItem at position $position: ${item.walletName}, Balance: ${item.walletBalance}")
                (holder as WalletViewHolder).bind(item)
            }
            is TransactionItem -> {
                Log.d(TAG, "Binding TransactionItem at position $position: ${item.categoryName}, Amount: ${item.amount}")
                (holder as TransactionViewHolder).bind(item)
            }
            else -> {
                Log.e(TAG, "Unexpected item type at $position")
                throw IllegalArgumentException("Unexpected item type at $position")
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${items.size}")
        return items.size
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: HomeWalletItem) {
            Log.d(TAG, "WalletViewHolder.bind: ${item.walletName}, Balance: ${item.walletBalance}")
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
            val nameText = itemView.findViewById<TextView>(R.id.txtWalletName)
            val balanceText = itemView.findViewById<TextView>(R.id.txtAmount)
            val dateText = itemView.findViewById<TextView>(R.id.txtRelativeDay)

            iconView.setImageResource(R.drawable.ic_account_balance_wallet_24)
            nameText.text = item.walletName
            balanceText.text = "R${item.walletBalance}"
            dateText.text = item.relativeDate
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TransactionItem) {
            Log.d(TAG, "TransactionViewHolder.bind: ${item.categoryName}, Amount: ${item.amount}")
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
