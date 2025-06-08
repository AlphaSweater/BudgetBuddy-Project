package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying and selecting category icons
 * @param onIconSelected Callback when an icon is selected
 */
class IconAdapter(
    private val onIconSelected: (IconItem) -> Unit
) : BaseAdapter<IconItem, IconAdapter.IconViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_category_icon
        ) { IconViewHolder(it) }
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(items[position])
        holder.setSelected(position == selectedPosition)
        holder.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onIconSelected(items[position])
        }
    }

    class IconViewHolder(
        itemView: View
    ) : BaseViewHolder<IconItem>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconView)
        private val selectedIndicator: ImageView = itemView.findViewById(R.id.selectedIndicator)

        override fun bind(item: IconItem) {
            iconView.setImageResource(item.iconResourceId)
        }

        fun setSelected(isSelected: Boolean) {
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        fun setOnClickListener(onClick: () -> Unit) {
            itemView.setOnClickListener { onClick() }
        }
    }
} 