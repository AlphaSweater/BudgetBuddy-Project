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

package com.synaptix.budgetbuddy.presentation.ui.main.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems

class BudgetMainAdapter (private val budgetItems: List<BudgetReportListItems.BudgetItem>,
                         private val onClick: (BudgetReportListItems.BudgetItem) -> Unit // <-- Add this
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BUDGET = 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (budgetItems[position]) {
            is BudgetReportListItems.BudgetItem -> VIEW_TYPE_BUDGET
            else -> throw IllegalArgumentException("Unsupported item type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BUDGET -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_current_budget, parent, false) // Make sure this is not your fragment layout!
                BudgetCardViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int = budgetItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val budgetItem = budgetItems[position]) {
            is BudgetReportListItems.BudgetItem -> (holder as BudgetCardViewHolder).bind(budgetItem, onClick)
            else -> throw IllegalArgumentException("Unsupported item type at position $position")
        }
    }

    class BudgetCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.budgetIcon)
        private val titleView: TextView = itemView.findViewById(R.id.budgetTitle)
        private val statusView: TextView = itemView.findViewById(R.id.budgetStatus)

        fun bind(item: BudgetReportListItems.BudgetItem, onClick: (BudgetReportListItems.BudgetItem) -> Unit) {
            iconView.setImageResource(item.categoryIcon)
            titleView.text = item.title
            statusView.text = item.status
            itemView.setOnClickListener { onClick(item) }
        }
    }
}