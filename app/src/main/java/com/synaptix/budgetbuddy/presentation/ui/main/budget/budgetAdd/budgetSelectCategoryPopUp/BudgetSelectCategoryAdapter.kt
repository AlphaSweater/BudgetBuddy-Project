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

/**
 * Adapter for displaying a list of categories in a RecyclerView.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses a dedicated ViewHolder class
 * 3. Handles category selection through a callback
 * 4. Uses the Category's built-in isSelected field for selection state
 *
 * @param onSelectionChanged Callback function that is triggered when category selection changes
 */
class BudgetSelectCategoryAdapter(
    private val onSelectionChanged: (List<Category>) -> Unit
) : BaseAdapter<Category, BudgetSelectCategoryAdapter.CategoryViewHolder>() {

    /**
     * Creates a new ViewHolder instance for category items.
     * Uses the standard item_category layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_budget_category
        ) { CategoryViewHolder(it) }
    }

    /**
     * Updates the adapter's data with a new list and selection state
     * @param newCategories The new list of categories
     * @param selectedCategories The list of currently selected categories
     */
    fun submitList(newCategories: List<Category>, selectedCategories: List<Category>) {
        val selectedIds = selectedCategories.map { it.id }.toSet()
        val updatedCategories = newCategories.map { category ->
            category.copy(isSelected = selectedIds.contains(category.id))
        }
        super.submitList(updatedCategories)
    }

    /**
     * Returns the list of currently selected categories
     */
    fun getSelectedCategories(): List<Category> {
        return items.filter { it.isSelected }
    }

    /**
     * Updates the selection state of a category and notifies listeners
     * @param category The category to update
     * @param isSelected The new selection state
     */
    private fun updateCategorySelection(category: Category, isSelected: Boolean) {
        val updatedCategories = items.map { 
            if (it.id == category.id) it.copy(isSelected = isSelected) else it 
        }
        submitList(updatedCategories)
        onSelectionChanged(getSelectedCategories())
    }

    /**
     * Toggles selection state for all categories
     * @param selectAll If true, selects all categories; if false, deselects all
     */
    fun toggleSelectAll(selectAll: Boolean) {
        val updatedCategories = items.map { it.copy(isSelected = selectAll) }
        submitList(updatedCategories)
        onSelectionChanged(getSelectedCategories())
    }

    /**
     * Checks if all items are currently selected
     * @return true if all items are selected, false otherwise
     */
    fun areAllItemsSelected(): Boolean {
        return items.isNotEmpty() && items.all { it.isSelected }
    }

    /**
     * ViewHolder class for category items.
     * Responsible for binding category data to the view and handling selection events.
     */
    inner class CategoryViewHolder(itemView: View) : BaseViewHolder<Category>(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkSelect)

        /**
         * Binds category data to the view.
         * Sets the category icon, name, and selection state.
         * Handles click events for both the item and checkbox.
         */
        override fun bind(item: Category) {
            val context = itemView.context

            categoryName.text = item.name
            categoryIcon.setImageResource(item.icon)
            val colorInt = ContextCompat.getColor(context, item.color)
            categoryIcon.setColorFilter(colorInt)
            checkBox.isChecked = item.isSelected

            itemView.setOnClickListener {
                updateCategorySelection(item, !item.isSelected)
            }

            checkBox.setOnClickListener {
                updateCategorySelection(item, checkBox.isChecked)
            }
        }
    }
}
