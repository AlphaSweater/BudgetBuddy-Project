package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetReport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.ListItem

class BudgetAdapter : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_TRANSACTION = 0
        private const val TYPE_CATEGORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.TransactionItem -> TYPE_TRANSACTION
            is ListItem.CategoryItem -> TYPE_CATEGORY
            // optional but safe
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    //This calls the items (please rename the items to match that one is a date item and the other is the transaction category
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TRANSACTION -> {
                //Data Item
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_date, parent, false)
                TransactionViewHolder(view)
            }
            TYPE_CATEGORY -> {
                //Transaction Item
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget_report, parent, false)
                CategoryViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    //Calls the ItemList
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item)
            is ListItem.CategoryItem -> (holder as CategoryViewHolder).bind(item)
        }
    }

    //Works with the ItemList in the core package, here the data is called (i think)
    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ListItem.TransactionItem) {
            val dayText = itemView.findViewById<TextView>(R.id.txtDay)
            val numberDayText = itemView.findViewById<TextView>(R.id.txtNumberDay)
            val dateText = itemView.findViewById<TextView>(R.id.txtMonthYear)
            val amountText = itemView.findViewById<TextView>(R.id.txtAmount)

            dayText.text = item.day
            numberDayText.text = item.numberDay
            dateText.text = item.monthYear
            amountText.text = item.amount
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ListItem.CategoryItem) {
            itemView.findViewById<TextView>(R.id.txtCategoryName).text = item.name
            itemView.findViewById<TextView>(R.id.txtTransactions).text = item.transactions
            itemView.findViewById<TextView>(R.id.txtAmount).text = item.amount
            itemView.findViewById<TextView>(R.id.txtDate).text = item.date
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}