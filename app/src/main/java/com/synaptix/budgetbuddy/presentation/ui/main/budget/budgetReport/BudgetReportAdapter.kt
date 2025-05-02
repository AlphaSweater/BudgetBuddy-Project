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

class BudgetReportAdapter(private val items: List<BudgetReportListItems>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_CATEGORY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is BudgetReportListItems.DateHeader -> VIEW_TYPE_HEADER
            is BudgetReportListItems.TransactionItem -> VIEW_TYPE_TRANSACTION
            is BudgetReportListItems.CategoryItems -> VIEW_TYPE_CATEGORY
            else -> throw IllegalArgumentException("Invalid item type at position $position")

        }
    }
    //took out if else statement this could be broken so note that
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_TRANSACTION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget_report, parent, false)
                CategoryViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun getItemCount() = items.size

    //Calls the ItemList
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is BudgetReportListItems.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is BudgetReportListItems.TransactionItem -> (holder as TransactionViewHolder).bind(item)
            is BudgetReportListItems.CategoryItems -> (holder as CategoryViewHolder).bind(item)
            else -> throw IllegalArgumentException("Unexpected item type at position $position: ${item::class.simpleName}")
        }
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BudgetReportListItems.DateHeader) {
            itemView.findViewById<TextView>(R.id.textDayNumber).text = item.dateNumber
            itemView.findViewById<TextView>(R.id.textRelativeDate).text = item.relativeDate
            itemView.findViewById<TextView>(R.id.textMonthYearDate).text = item.monthYearDate
            itemView.findViewById<TextView>(R.id.textTotalAmount).text = "R ${item.amountTotal}"
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BudgetReportListItems.TransactionItem) {
            val iconContainer = itemView.findViewById<LinearLayout>(R.id.iconCategoryContainer)
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)

            // Convert resource ID to actual color
            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)

            // Set the background circle color
            val background = iconContainer.background.mutate() as GradientDrawable
            background.setColor(resolvedColor)

            // Set the icon
            iconView.setImageResource(item.categoryIcon)

            itemView.findViewById<TextView>(R.id.textCategoryName).text = item.categoryName
            itemView.findViewById<TextView>(R.id.textWalletName).text = item.walletName

            if (item.note == null) {
                itemView.findViewById<LinearLayout>(R.id.rowNote).visibility = View.GONE
            } else {
                itemView.findViewById<LinearLayout>(R.id.rowNote).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.textNote).text = item.note
            }
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BudgetReportListItems.CategoryItems) {
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
            val iconContainer = itemView.findViewById<LinearLayout>(R.id.iconCategoryContainer)

            //Convert resource ID to actual color
            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)

            //set icon
            iconView.setImageResource(item.categoryIcon)

            itemView.findViewById<TextView>(R.id.txtCategoryName).text = item.categoryName
            itemView.findViewById<TextView>(R.id.txtTransactions).text = "${item.transactionCount} transactions"
            itemView.findViewById<TextView>(R.id.txtAmount).text = item.amount
            itemView.findViewById<TextView>(R.id.txtDate).text = item.relativeDate
        }
    }

}