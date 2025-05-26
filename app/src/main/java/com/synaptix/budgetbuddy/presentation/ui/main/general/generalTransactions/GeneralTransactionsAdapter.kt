package com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions

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
import com.synaptix.budgetbuddy.core.model.BudgetListItems

//class GeneralTransactionsAdapter(private val items: List<BudgetListItems>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    companion object {
//        private const val VIEW_TYPE_HEADER = 0
//        private const val VIEW_TYPE_TRANSACTION = 1
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return when (items[position]) {
//            is BudgetListItems.BudgetDateHeader -> VIEW_TYPE_HEADER
//            is BudgetListItems.BudgetTransactionItem -> VIEW_TYPE_TRANSACTION
//            else -> throw IllegalArgumentException("Unsupported item type at position $position")
//        }
//    }
//    //took out if else statement this could be broken so note that
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when (viewType) {
//            GeneralTransactionsAdapter.VIEW_TYPE_HEADER -> {
//                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
//                DateHeaderViewHolder(view)
//            }
//            GeneralTransactionsAdapter.VIEW_TYPE_TRANSACTION -> {
//                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_transaction, parent, false)
//                TransactionViewHolder(view)
//            }
//            else -> throw IllegalArgumentException("Unknown view type")
//        }
//    }
//    override fun getItemCount() = items.size
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (val item = items[position]) {
//            is BudgetListItems.BudgetDateHeader -> (holder as DateHeaderViewHolder).bind(item)
//            is BudgetListItems.BudgetTransactionItem -> (holder as TransactionViewHolder).bind(item)
//            else -> throw IllegalArgumentException("Unsupported item type at position $position")
//        }
//    }
//
//    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(item: BudgetListItems.BudgetDateHeader) {
//            itemView.findViewById<TextView>(R.id.textDayNumber).text = item.dateNumber
//            itemView.findViewById<TextView>(R.id.textRelativeDate).text = item.relativeDate
//            itemView.findViewById<TextView>(R.id.textMonthYearDate).text = item.monthYearDate
//            itemView.findViewById<TextView>(R.id.textTotalAmount).text = "R ${item.amountTotal}"
//        }
//    }
//
//    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(item: BudgetListItems.BudgetTransactionItem) {
//            val iconContainer = itemView.findViewById<LinearLayout>(R.id.iconCategoryContainer)
//            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
//
//            // Convert resource ID to actual color
//            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
//
//            // Set the background circle color
//            val background = iconContainer.background.mutate() as GradientDrawable
//            background.setColor(resolvedColor)
//
//            // Set the icon
//            iconView.setImageResource(item.categoryIcon)
//
//            itemView.findViewById<TextView>(R.id.textCategoryName).text = item.categoryName
//            itemView.findViewById<TextView>(R.id.textWalletName).text = item.name
//
//            if (item.note == null) {
//                itemView.findViewById<LinearLayout>(R.id.rowNote).visibility = View.GONE
//            } else {
//                itemView.findViewById<LinearLayout>(R.id.rowNote).visibility = View.VISIBLE
//                itemView.findViewById<TextView>(R.id.textNote).text = item.note
//            }
//        }
//    }
//}