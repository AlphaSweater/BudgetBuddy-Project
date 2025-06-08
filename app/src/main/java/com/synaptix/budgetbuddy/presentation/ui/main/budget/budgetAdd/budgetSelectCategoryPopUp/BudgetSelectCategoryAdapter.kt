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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectCategoryPopUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp.TransactionSelectCategoryAdapter

// Adapter class for displaying a list of categories in RecyclerView
class BudgetSelectCategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
) : BaseAdapter<Category, BudgetSelectCategoryAdapter.CategoryViewHolder>() {

    /**
     * Creates a new ViewHolder instance for category items.
     * Uses the standard item_category layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_transaction_category
        ) { CategoryViewHolder(it) }
    }

    /**
     * ViewHolder class for category items.
     * Responsible for binding category data to the view and handling click events.
     */
    inner class CategoryViewHolder(itemView: View) : BaseViewHolder<Category>(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)

        /**
         * Binds category data to the view.
         * Sets the category icon, name, and applies the category's color to the icon.
         */
        override fun bind(item: Category) {
            val context = itemView.context

            categoryName.text = item.name
            categoryIcon.setImageResource(item.icon)
            val colorInt = ContextCompat.getColor(context, item.color)
            categoryIcon.setColorFilter(colorInt)

            itemView.setOnClickListener {
                onCategoryClick(item)
            }
        }
    }
}
