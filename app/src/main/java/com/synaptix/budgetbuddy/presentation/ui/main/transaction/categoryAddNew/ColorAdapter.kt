package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying and selecting category colors
 * @param onColorSelected Callback when a color is selected
 */
class ColorAdapter(
    private val onColorSelected: (ColorItem) -> Unit
) : BaseAdapter<ColorItem, ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_category_color
        ) { ColorViewHolder(it) }
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(items[position])
        holder.setSelected(position == selectedPosition)
        holder.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onColorSelected(items[position])
        }
    }

    class ColorViewHolder(
        itemView: View
    ) : BaseViewHolder<ColorItem>(itemView) {
        private val colorView: ImageView = itemView.findViewById(R.id.colorView)
        private val selectedIndicator: ImageView = itemView.findViewById(R.id.selectedIndicator)

        override fun bind(item: ColorItem) {
            val context = itemView.context
            colorView.setColorFilter(ContextCompat.getColor(context, item.colorResourceId))
        }

        fun setSelected(isSelected: Boolean) {
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        fun setOnClickListener(onClick: () -> Unit) {
            itemView.setOnClickListener { onClick() }
        }
    }
} 