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
    private val onCategoryClick: ((Category) -> Unit)? = null
) : BaseAdapter<ReportListItems, BaseAdapter.BaseViewHolder<ReportListItems>>() {

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ReportListItems.ReportCategoryItem -> VIEW_TYPE_CATEGORY
        else -> throw IllegalArgumentException("Invalid item type at position $position")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ReportListItems> {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_category
            ) { CategoryViewHolder(it, onCategoryClick) }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
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
