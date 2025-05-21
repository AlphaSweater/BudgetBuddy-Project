//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.HomeListItems.HomeCategoryItem
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying budget report items in the budget report screen.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses dedicated ViewHolder classes for each item type
 * 3. Handles multiple view types (header, transaction, category)
 * 4. Displays formatted data with proper styling
 */
class BudgetReportAdapter(
    private val onTransactionClick: ((Transaction) -> Unit)? = null,
    private val onCategoryClick: ((Category) -> Unit)? = null
) : BaseAdapter<BudgetListItems, BaseAdapter.BaseViewHolder<BudgetListItems>>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_CATEGORY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is BudgetListItems.BudgetDateHeader -> VIEW_TYPE_HEADER
            is BudgetListItems.BudgetTransactionItem -> VIEW_TYPE_TRANSACTION
            is BudgetListItems.BudgetCategoryItem -> VIEW_TYPE_CATEGORY
            else -> throw IllegalArgumentException("Invalid item type at position $position")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<BudgetListItems> {
        return when (viewType) {
            VIEW_TYPE_HEADER -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_date_header
            ) {
                DateHeaderViewHolder(it)
            }
            VIEW_TYPE_TRANSACTION -> createViewHolder(parent, R.layout.item_home_transaction) { 
                TransactionViewHolder(it, onTransactionClick)
            }
            VIEW_TYPE_CATEGORY -> createViewHolder(parent, R.layout.item_home_category) { 
                CategoryViewHolder(it, onCategoryClick)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BudgetListItems>, position: Int) {
        when (val item = items[position]) {
            is BudgetListItems.BudgetDateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is BudgetListItems.BudgetTransactionItem -> (holder as TransactionViewHolder).bind(item)
            is BudgetListItems.BudgetCategoryItem -> (holder as CategoryViewHolder).bind(item)
            else -> throw IllegalArgumentException("Unexpected item type at position $position: ${item::class.simpleName}")
        }
    }

    class DateHeaderViewHolder(
        itemView: View
    ) : BaseViewHolder<BudgetListItems>(itemView) {
        private val dayNumber: TextView = itemView.findViewById(R.id.textDayNumber)
        private val relativeDate: TextView = itemView.findViewById(R.id.textRelativeDate)
        private val monthYearDate: TextView = itemView.findViewById(R.id.textMonthYearDate)
        private val totalAmount: TextView = itemView.findViewById(R.id.textTotalAmount)

        override fun bind(item: BudgetListItems) {

            if (item !is BudgetListItems.BudgetDateHeader) return

            dayNumber.text = item.dateNumber
            relativeDate.text = item.relativeDate
            monthYearDate.text = item.monthYearDate
            totalAmount.text = "R ${item.amountTotal}"
        }
    }

    class TransactionViewHolder(
        itemView: View,
        private val onClick: ((Transaction) -> Unit)?
    ) : BaseViewHolder<BudgetListItems>(itemView) {
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val categoryName: TextView = itemView.findViewById(R.id.textCategoryName)
        private val walletName: TextView = itemView.findViewById(R.id.textWalletName)
        private val noteContainer: LinearLayout = itemView.findViewById(R.id.rowNote)
        private val noteText: TextView = itemView.findViewById(R.id.textNote)

        override fun bind(item: BudgetListItems) {
            if (item !is BudgetListItems.BudgetTransactionItem) return

            // Set category icon and color
            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)
            iconView.setImageResource(item.categoryIcon)

            // Set text fields
            categoryName.text = item.categoryName
            walletName.text = item.transaction.wallet?.walletName

            // Handle note visibility
            if (item.transaction.note == null) {
                noteContainer.visibility = View.GONE
            } else {
                noteContainer.visibility = View.VISIBLE
                noteText.text = item.transaction.note
            }
        }
    }

    class CategoryViewHolder(
        itemView: View,
        private val onClick: ((Category) -> Unit)?
    ) : BaseViewHolder<BudgetListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val transactionCount: TextView = itemView.findViewById(R.id.txtTransactions)
        private val amount: TextView = itemView.findViewById(R.id.txtAmount)
        private val date: TextView = itemView.findViewById(R.id.txtDate)

        override fun bind(item: BudgetListItems) {
            if (item !is BudgetListItems.BudgetCategoryItem) return

            // Set category icon and color
            val resolvedColor = ContextCompat.getColor(itemView.context, item.category.categoryColor)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)
            iconView.setImageResource(item.category.categoryIcon)

            // Set text fields
            categoryName.text = item.category.categoryName
            transactionCount.text = "${item.transactionCount} transactions"
            amount.text = item.amount
            date.text = item.relativeDate
        }
    }
}