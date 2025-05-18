package com.synaptix.budgetbuddy.presentation.ui.common

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.synaptix.budgetbuddy.R

/**
 * Template adapter that demonstrates how to implement a standardized adapter
 * This template shows common patterns and best practices for RecyclerView adapters
 * 
 * @param onItemClick Callback for when an item is clicked
 * @param onItemLongClick Optional callback for when an item is long-pressed
 */
class TemplateAdapter(
    private val onItemClick: (TemplateItem) -> Unit,
    private val onItemLongClick: ((TemplateItem) -> Unit)? = null
) : BaseAdapter<TemplateItem, TemplateAdapter.TemplateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_home_transaction
        ) { view -> TemplateViewHolder(view, onItemClick, onItemLongClick) }
    }

    class TemplateViewHolder(
        itemView: View,
        private val onItemClick: (TemplateItem) -> Unit,
        private val onItemLongClick: ((TemplateItem) -> Unit)?
    ) : BaseViewHolder<TemplateItem>(itemView) {
        
        // View references - use meaningful names
        private val iconView: ImageView = itemView.findViewById(R.id.iconView)
//        private val titleText: TextView = itemView.findViewById(R.id.titleView)
//        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionView)

        override fun bind(item: TemplateItem) {
            // Set up views
            iconView.setImageResource(item.iconResId)
//            titleText.text = item.title
//            descriptionText.text = item.description

            // Set up click listeners
            itemView.setOnClickListener { onItemClick(item) }
            onItemLongClick?.let { longClick ->
                itemView.setOnLongClickListener { 
                    longClick(item)
                    true
                }
            }
        }
    }
}

/**
 * Data class representing an item in the template adapter
 * Using a data class ensures equals() and hashCode() are properly implemented
 */
data class TemplateItem(
    val id: String, // Unique identifier for DiffUtil
    val title: String,
    val description: String,
    val iconResId: Int
) 