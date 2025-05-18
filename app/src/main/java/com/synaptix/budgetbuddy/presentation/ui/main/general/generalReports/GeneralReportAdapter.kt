package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

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

class GeneralReportAdapter(private val items: List<BudgetReportListItems>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LABEL = 0
        private const val VIEW_TYPE_CATEGORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is BudgetReportListItems.LabelItems -> VIEW_TYPE_LABEL
            is BudgetReportListItems.CategoryItems -> VIEW_TYPE_CATEGORY
            else -> throw IllegalArgumentException("Invalid item type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LABEL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_category, parent, false)
                LabelViewHolder(view)
            }
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_category, parent, false)
                CategoryViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is BudgetReportListItems.LabelItems -> (holder as LabelViewHolder).bind(item)
            is BudgetReportListItems.CategoryItems -> (holder as CategoryViewHolder).bind(item)
            else -> throw IllegalArgumentException("Unexpected item type at position $position: ${item::class.simpleName}")
        }
    }

    override fun getItemCount(): Int = items.size

    class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BudgetReportListItems.LabelItems) {
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
            val iconContainer = itemView.findViewById<LinearLayout>(R.id.iconCategoryContainer)

            val resolvedColor = ContextCompat.getColor(itemView.context, item.labelColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)

            iconView.setImageResource(item.labelIcon)

            itemView.findViewById<TextView>(R.id.txtCategoryName).text = item.labelName
            itemView.findViewById<TextView>(R.id.txtTransactions).text = "${item.transactionCount} transactions"
            itemView.findViewById<TextView>(R.id.txtAmount).text = item.amount
            itemView.findViewById<TextView>(R.id.txtDate).text = item.relativeDate
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BudgetReportListItems.CategoryItems) {
            val iconView = itemView.findViewById<ImageView>(R.id.iconCategory)
            val iconContainer = itemView.findViewById<LinearLayout>(R.id.iconCategoryContainer)

            val resolvedColor = ContextCompat.getColor(itemView.context, item.categoryColour)
            (iconContainer.background.mutate() as GradientDrawable).setColor(resolvedColor)

            iconView.setImageResource(item.categoryIcon)

            itemView.findViewById<TextView>(R.id.txtCategoryName).text = item.categoryName
            itemView.findViewById<TextView>(R.id.txtTransactions).text = "${item.transactionCount} transactions"
            itemView.findViewById<TextView>(R.id.txtAmount).text = item.amount
            itemView.findViewById<TextView>(R.id.txtDate).text = item.relativeDate
        }
    }
}
