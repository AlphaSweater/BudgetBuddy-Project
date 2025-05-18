package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R

class IconAdapter(
    private val onIconSelected: (IconItem) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    private var icons: List<IconItem> = emptyList()
    private var selectedPosition = -1

    fun submitList(newIcons: List<IconItem>) {
        icons = newIcons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = icons.size

    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconView)
        private val selectedIndicator: ImageView = itemView.findViewById(R.id.selectedIndicator)

        fun bind(icon: IconItem, isSelected: Boolean) {
            iconView.setImageResource(icon.iconResourceId)
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onIconSelected(icon)
            }
        }
    }
} 