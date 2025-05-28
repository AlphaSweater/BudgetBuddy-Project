package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

class GeneralReportAdapter(
    private val onTransactionClick: ((Transaction) -> Unit)? = null,
    private val onCategoryClick: ((Category) -> Unit)? = null
) : BaseAdapter<ReportListItems, BaseAdapter.BaseViewHolder<ReportListItems>>() {

    companion object {
        private const val VIEW_TYPE_TRANSACTION = 0
        private const val VIEW_TYPE_CATEGORY = 1
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ReportListItems.ReportTransactionItem -> VIEW_TYPE_TRANSACTION
        is ReportListItems.ReportCategoryItem -> VIEW_TYPE_CATEGORY
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ReportListItems> {
        return when (viewType) {
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

    class TransactionViewHolder(
        itemView: View,
        private val onClick: ((Transaction) -> Unit)?
    ) : BaseViewHolder<ReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val categoryName: TextView = itemView.findViewById(R.id.textCategoryName)
        private val walletIcon: ImageView = itemView.findViewById(R.id.iconWallet)
        private val walletName: TextView = itemView.findViewById(R.id.textWalletName)
        private val noteContainer: View = itemView.findViewById(R.id.rowNote)
        private val noteText: TextView = itemView.findViewById(R.id.textNote)
        private val amountText: TextView = itemView.findViewById(R.id.textAmount)
        private val dateText: TextView = itemView.findViewById(R.id.textDate)

        override fun bind(item: ReportListItems) {
            if (item !is ReportListItems.ReportTransactionItem) return

            val resolvedColor = ContextCompat.getColor(itemView.context, item.transaction.category.color)

            iconView.setImageResource(item.transaction.category.icon)
            iconView.setColorFilter(resolvedColor)
            categoryName.text = item.transaction.category.name

            walletIcon.setImageResource(R.drawable.ic_ui_wallet)
            walletName.text = item.transaction.wallet.name

            if (item.transaction.note.isNotBlank()) {
                noteContainer.visibility = View.VISIBLE
                noteText.text = item.transaction.note
            } else {
                noteContainer.visibility = View.GONE
            }

            val amountFormatted = String.format("R %.2f", item.transaction.amount)
            amountText.text = amountFormatted

            val colorRes = if (item.transaction.category.type.equals("INCOME", ignoreCase = true)) {
                R.color.profit_green
            } else {
                R.color.expense_red
            }

            amountText.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
            dateText.text = item.relativeDate

            itemView.setOnClickListener { onClick?.invoke(item.transaction) }
        }
    }

    class CategoryViewHolder(
        itemView: View,
        private val onClick: ((Category) -> Unit)?
    ) : BaseViewHolder<ReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val nameText: TextView = itemView.findViewById(R.id.categoryName)
        private val transactionsText: TextView = itemView.findViewById(R.id.categoryTransactions)
        private val amountText: TextView = itemView.findViewById(R.id.categoryAmount)
        private val dateText: TextView = itemView.findViewById(R.id.categoryDate)

        override fun bind(item: ReportListItems) {
            if (item !is ReportListItems.ReportCategoryItem) return

            val resolvedColor = ContextCompat.getColor(itemView.context, item.category.color)

            iconView.setImageResource(item.category.icon)
            iconView.setColorFilter(resolvedColor)
            nameText.text = item.category.name
            transactionsText.text = "${item.transactionCount} transactions"
            amountText.text = item.amount
            dateText.text = item.relativeDate

            itemView.setOnClickListener { onClick?.invoke(item.category) }
        }
    }
}

sealed class ReportListItems {
    data class ReportTransactionItem(
        val transaction: Transaction,
        val relativeDate: String
    ) : ReportListItems()

    data class ReportCategoryItem(
        val category: Category,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String
    ) : ReportListItems()
}
