package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems.*
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

class HomeAdapter(
    private val onWalletClick: ((HomeWalletItem) -> Unit)? = null,
    private val onTransactionClick: ((TransactionItem) -> Unit)? = null,
    private val onCategoryClick: ((CategoryItems) -> Unit)? = null
) : BaseAdapter<BudgetReportListItems, BaseAdapter.BaseViewHolder<BudgetReportListItems>>() {

    companion object {
        private const val VIEW_TYPE_WALLET = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_CATEGORY = 2
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomeWalletItem -> VIEW_TYPE_WALLET
        is TransactionItem -> VIEW_TYPE_TRANSACTION
        is CategoryItems -> VIEW_TYPE_CATEGORY
        else -> {}
    } as Int

    override fun onCreateViewHolder(
        parent: ViewGroup, 
        viewType: Int
    ): BaseViewHolder<BudgetReportListItems> {
        return when (viewType) {
            VIEW_TYPE_WALLET -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_wallet
            ) { WalletViewHolder(it, onWalletClick) }
            VIEW_TYPE_TRANSACTION -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_transaction
            ) { TransactionViewHolder(it, onTransactionClick) }
            VIEW_TYPE_CATEGORY -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_category
            ) { CategoryViewHolder(it, onCategoryClick) }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    class WalletViewHolder(
        itemView: View,
        private val onClick: ((HomeWalletItem) -> Unit)?
    ) : BaseViewHolder<BudgetReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.imgWalletIcon)
        private val nameText: TextView = itemView.findViewById(R.id.txtWalletName)
        private val balanceText: TextView = itemView.findViewById(R.id.txtWalletBalance)
        private val dateText: TextView = itemView.findViewById(R.id.txtRelativeDay)

        override fun bind(item: BudgetReportListItems) {
            if (item !is HomeWalletItem) return
            
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
    ) : BaseViewHolder<BudgetReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val nameText: TextView = itemView.findViewById(R.id.textCategoryName)
        private val amountText: TextView = itemView.findViewById(R.id.text_amount)
        private val dateText: TextView = itemView.findViewById(R.id.text_date)
        private val walletText: TextView = itemView.findViewById(R.id.textWalletName)

        override fun bind(item: BudgetReportListItems) {
            if (item !is TransactionItem) return
            
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
    ) : BaseViewHolder<BudgetReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val nameText: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val transactionsText: TextView = itemView.findViewById(R.id.txtTransactions)
        private val amountText: TextView = itemView.findViewById(R.id.txtAmount)
        private val dateText: TextView = itemView.findViewById(R.id.txtDate)

        override fun bind(item: BudgetReportListItems) {
            if (item !is CategoryItems) return
            
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
