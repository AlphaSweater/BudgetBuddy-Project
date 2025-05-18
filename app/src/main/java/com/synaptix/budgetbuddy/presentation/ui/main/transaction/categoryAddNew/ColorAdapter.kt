package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.CategoryColor

class ColorAdapter(
    private val onColorSelected: (CategoryColor) -> Unit
) : ListAdapter<CategoryColor, ColorAdapter.ColorViewHolder>(ColorDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = getItem(position)
        holder.bind(color, position == selectedPosition)
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            val newPosition = holder.adapterPosition
            if (newPosition != RecyclerView.NO_POSITION) {
                selectedPosition = newPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onColorSelected(color)
            }
        }
    }

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.colorCardView)
        private val colorView: ImageView = itemView.findViewById(R.id.imgColor)

        fun bind(color: CategoryColor, isSelected: Boolean) {
            colorView.setColorFilter(itemView.context.getColor(color.colorValue))
            cardView.isChecked = isSelected
        }
    }

    private class ColorDiffCallback : DiffUtil.ItemCallback<CategoryColor>() {
        override fun areItemsTheSame(oldItem: CategoryColor, newItem: CategoryColor): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryColor, newItem: CategoryColor): Boolean {
            return oldItem == newItem
        }
    }
} 