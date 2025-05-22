package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying a list of categories in a RecyclerView.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses a dedicated ViewHolder class
 * 3. Handles item click events through a callback
 * 4. Handles category icon and color display
 *
 * @param onCategoryClick Callback function that is triggered when a category item is clicked
 */
class TransactionSelectCategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
) : BaseAdapter<Category, TransactionSelectCategoryAdapter.CategoryViewHolder>() {

    /**
     * Creates a new ViewHolder instance for category items.
     * Uses the standard item_category layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_category
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