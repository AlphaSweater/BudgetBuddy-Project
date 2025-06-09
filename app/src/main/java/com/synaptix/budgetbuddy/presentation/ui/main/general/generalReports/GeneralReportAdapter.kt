package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying report items in the general reports screen.
 * 
 * This adapter handles multiple view types:
 * 1. Category items - Shows category summary with transaction count and amount
 * 2. Label items - Shows label summary with transaction count and amount
 * 
 * Each item type has its own ViewHolder and layout resource.
 * The adapter follows the standard pattern for RecyclerView adapters in the app.
 * 
 * @param onCategoryClick Optional callback for category item clicks
 * @param onLabelClick Optional callback for label item clicks
 */
class GeneralReportAdapter(
    private val onCategoryClick: ((Category) -> Unit)? = null,
    private val onLabelClick: ((Label) -> Unit)? = null
) : BaseAdapter<ReportListItems, BaseAdapter.BaseViewHolder<ReportListItems>>() {

    companion object {
        private const val VIEW_TYPE_CATEGORY = 0
        private const val VIEW_TYPE_LABEL = 1
    }

    /**
     * Determines the view type for the item at the given position.
     * This is used by the RecyclerView to create the appropriate ViewHolder.
     * 
     * @param position The position of the item in the list
     * @return An integer representing the view type
     */
    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ReportListItems.ReportCategoryItem -> VIEW_TYPE_CATEGORY
        is ReportListItems.ReportLabelItem -> VIEW_TYPE_LABEL
    }

    /**
     * Creates the appropriate ViewHolder based on the view type.
     * Each view type has its own layout resource and ViewHolder implementation.
     * 
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ReportListItems> {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_category
            ) { CategoryViewHolder(it, onCategoryClick) }

            VIEW_TYPE_LABEL -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_label
            ) { LabelViewHolder(it, onLabelClick) }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    /**
     * ViewHolder for category items in the reports screen.
     * Displays category icon, name, transaction count, amount, and date.
     * Applies category color to the icon background.
     * 
     * @param itemView The view for this ViewHolder
     * @param onClick Optional callback for category item clicks
     */
    class CategoryViewHolder(
        itemView: View,
        private val onClick: ((Category) -> Unit)?
    ) : BaseViewHolder<ReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val nameText: TextView = itemView.findViewById(R.id.categoryName)
        private val transactionsText: TextView = itemView.findViewById(R.id.categoryTransactions)
        private val amountText: TextView = itemView.findViewById(R.id.categoryAmount)
        private val dateText: TextView = itemView.findViewById(R.id.categoryDate)

        /**
         * Binds category data to the view.
         * Sets the category icon and color, name, transaction count, amount, and date.
         * 
         * @param item The ReportListItems object containing category data
         */
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

    /**
     * ViewHolder for label items in the reports screen.
     * Displays label icon, name, transaction count, amount, and date.
     * 
     * @param itemView The view for this ViewHolder
     * @param onClick Optional callback for label item clicks
     */
    class LabelViewHolder(
        itemView: View,
        private val onClick: ((Label) -> Unit)?
    ) : BaseViewHolder<ReportListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.labelIcon)
        private val nameText: TextView = itemView.findViewById(R.id.labelName)
        private val transactionsText: TextView = itemView.findViewById(R.id.labelTransactions)
        private val amountText: TextView = itemView.findViewById(R.id.labelAmount)
        private val dateText: TextView = itemView.findViewById(R.id.labelDate)

        /**
         * Binds label data to the view.
         * Sets the label icon, name, transaction count, amount, and date.
         * 
         * @param item The ReportListItems object containing label data
         */
        override fun bind(item: ReportListItems) {
            if (item !is ReportListItems.ReportLabelItem) return

            // Use a default icon for labels
            iconView.setImageResource(R.drawable.ic_ui_label)
            nameText.text = item.label.name
            transactionsText.text = "${item.transactionCount} transactions"
            amountText.text = item.amount
            dateText.text = item.relativeDate

            itemView.setOnClickListener { onClick?.invoke(item.label) }
        }
    }
}

sealed class ReportListItems {
    data class ReportCategoryItem(
        val category: Category,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String
    ) : ReportListItems()

    data class ReportLabelItem(
        val label: Label,
        val transactionCount: Int,
        val amount: String,
        val relativeDate: String
    ) : ReportListItems()
}
