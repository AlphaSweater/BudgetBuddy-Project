package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.CategoryIcon

class IconAdapter(
    private val onIconSelected: (CategoryIcon) -> Unit
) : ListAdapter<CategoryIcon, IconAdapter.IconViewHolder>(IconDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_icon, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = getItem(position)
        holder.bind(icon, position == selectedPosition)
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            val newPosition = holder.adapterPosition
            if (newPosition != RecyclerView.NO_POSITION) {
                selectedPosition = newPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onIconSelected(icon)
            }
        }
    }

    class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.iconCardView)
        private val iconView: ImageView = itemView.findViewById(R.id.imgIcon)

        fun bind(icon: CategoryIcon, isSelected: Boolean) {
            iconView.setImageResource(icon.iconResourceId)
            cardView.isChecked = isSelected
        }
    }

    private class IconDiffCallback : DiffUtil.ItemCallback<CategoryIcon>() {
        override fun areItemsTheSame(oldItem: CategoryIcon, newItem: CategoryIcon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryIcon, newItem: CategoryIcon): Boolean {
            return oldItem == newItem
        }
    }
} 