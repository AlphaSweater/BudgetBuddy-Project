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
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.CategoryItems
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.HomeWalletItem
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.TransactionItem

class HomeAdapter(
    private val items: List<BudgetReportListItems>,
    private val onWalletClick: ((HomeWalletItem) -> Unit)? = null,
    private val onTransactionClick: ((TransactionItem) -> Unit)? = null,
    private val onCategoryClick: ((CategoryItems) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_WALLET = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_CATEGORY = 2
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomeWalletItem -> VIEW_TYPE_WALLET
        is TransactionItem -> VIEW_TYPE_TRANSACTION
        is CategoryItems -> VIEW_TYPE_CATEGORY
        else -> throw IllegalArgumentException("Invalid item type at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_WALLET -> WalletViewHolder(
                inflater.inflate(R.layout.item_home_wallet, parent, false),
                onWalletClick
            )
            VIEW_TYPE_TRANSACTION -> TransactionViewHolder(
                inflater.inflate(R.layout.item_transaction, parent, false),
                onTransactionClick
            )
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(
                inflater.inflate(R.layout.item_budget_report, parent, false),
                onCategoryClick
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeWalletItem -> (holder as WalletViewHolder).bind(item)
            is TransactionItem -> (holder as TransactionViewHolder).bind(item)
            is CategoryItems -> (holder as CategoryViewHolder).bind(item)
            else -> throw IllegalArgumentException("Unexpected item type at $position")
        }
    }

    override fun getItemCount(): Int = items.size

    class WalletViewHolder(
        itemView: View,
        private val onClick: ((HomeWalletItem) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val nameText: TextView = itemView.findViewById(R.id.txtWalletName)
        private val balanceText: TextView = itemView.findViewById(R.id.txtAmount)
        private val dateText: TextView = itemView.findViewById(R.id.txtRelativeDay)

        fun bind(item: HomeWalletItem) {
            iconView.setImageResource(R.drawable.ic_account_balance_wallet_24)
            nameText.text = item.walletName
            balanceText.text = "R${item.walletBalance}"
            dateText.text = item.relativeDate
            
            itemView.setOnClickListener { onClick?.invoke(item) }
        }
    }

    class TransactionViewHolder(
        itemView: View,
        private val onClick: ((TransactionItem) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val nameText: TextView = itemView.findViewById(R.id.textCategoryName)
        private val amountText: TextView = itemView.findViewById(R.id.text_amount)
        private val dateText: TextView = itemView.findViewById(R.id.text_date)
        private val walletText: TextView = itemView.findViewById(R.id.textWalletName)

        fun bind(item: TransactionItem) {
            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)

            iconView.setImageResource(item.categoryIcon)
            nameText.text = item.categoryName
            amountText.text = "R${item.amount}"
            dateText.text = item.relativeDate
            walletText.text = item.walletName
            
            itemView.setOnClickListener { onClick?.invoke(item) }
        }
    }

    class CategoryViewHolder(
        itemView: View,
        private val onClick: ((CategoryItems) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val nameText: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val transactionsText: TextView = itemView.findViewById(R.id.txtTransactions)
        private val amountText: TextView = itemView.findViewById(R.id.txtAmount)
        private val dateText: TextView = itemView.findViewById(R.id.txtDate)

        fun bind(item: CategoryItems) {
            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)

            iconView.setImageResource(item.categoryIcon)
            nameText.text = item.categoryName
            transactionsText.text = "${item.transactionCount} transactions"
            amountText.text = item.amount
            dateText.text = item.relativeDate
            
            itemView.setOnClickListener { onClick?.invoke(item) }
        }
    }
}
