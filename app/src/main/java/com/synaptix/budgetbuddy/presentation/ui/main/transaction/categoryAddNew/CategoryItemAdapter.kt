package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew.CategoryItem.ColorItem
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew.CategoryItem.IconItem

/**
 * Combined adapter for displaying and selecting category colors and icons.
 * Handles both color and icon selection with proper state management.
 * 
 * @param onItemSelected Callback when an item is selected
 */
class CategoryItemAdapter(
    private val onItemSelected: (CategoryItem) -> Unit
) : BaseAdapter<CategoryItem, CategoryItemAdapter.CategoryItemViewHolder>() {

    private var selectedItem: CategoryItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_category_item
        ) { CategoryItemViewHolder(it) }
    }

    override fun onBindViewHolder(holder: CategoryItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.setSelected(item == selectedItem)
        holder.setOnClickListener {
            val oldSelected = selectedItem
            selectedItem = item
            // Update both old and new positions
            oldSelected?.let { old ->
                val oldPosition = items.indexOf(old)
                if (oldPosition != -1) notifyItemChanged(oldPosition)
            }
            notifyItemChanged(position)
            onItemSelected(item)
        }
    }

    /**
     * Updates the selected item and notifies the adapter.
     * This is useful when the selection needs to be updated from outside.
     * 
     * @param item The item to select, or null to clear selection
     */
    fun setSelectedItem(item: CategoryItem?) {
        val oldSelected = selectedItem
        selectedItem = item
        // Update both old and new positions
        oldSelected?.let { old ->
            val oldPosition = items.indexOf(old)
            if (oldPosition != -1) notifyItemChanged(oldPosition)
        }
        item?.let { new ->
            val newPosition = items.indexOf(new)
            if (newPosition != -1) notifyItemChanged(newPosition)
        }
    }

    /**
     * ViewHolder for category items (colors and icons).
     * Handles the display and selection state of individual items.
     * 
     * @param itemView The view for this ViewHolder
     */
    class CategoryItemViewHolder(
        itemView: View
    ) : BaseViewHolder<CategoryItem>(itemView) {
        private val itemView: ImageView = itemView.findViewById(R.id.itemView)
        private val selectedIndicator: ImageView = itemView.findViewById(R.id.selectedIndicator)

        /**
         * Binds the item data to the view.
         * Sets the appropriate color or icon based on the item type.
         * 
         * @param item The CategoryItem to bind
         */
        override fun bind(item: CategoryItem) {
            val context = itemView.context
            when (item) {
                is ColorItem -> {
                    itemView.setColorFilter(ContextCompat.getColor(context, item.colorResourceId))
                }
                is IconItem -> {
                    itemView.setImageResource(item.iconResourceId)
                }
            }
        }

        /**
         * Updates the selection state of the item.
         * Shows or hides the selection indicator.
         * 
         * @param isSelected Whether this item is selected
         */
        fun setSelected(isSelected: Boolean) {
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        /**
         * Sets the click listener for this item.
         * 
         * @param onClick The callback to invoke when clicked
         */
        fun setOnClickListener(onClick: () -> Unit) {
            itemView.setOnClickListener { onClick() }
        }
    }
} 