package com.synaptix.budgetbuddy.presentation.ui.main.transaction.categoryAddNew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R

class ColorAdapter(
    private val onColorSelected: (ColorItem) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var colors: List<ColorItem> = emptyList()
    private var selectedPosition = -1

    fun submitList(newColors: List<ColorItem>) {
        colors = newColors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: ImageView = itemView.findViewById(R.id.colorView)
        private val selectedIndicator: ImageView = itemView.findViewById(R.id.selectedIndicator)

        fun bind(color: ColorItem, isSelected: Boolean) {
            val context = itemView.context
            colorView.setColorFilter(ContextCompat.getColor(context, color.colorResourceId))
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onColorSelected(color)
            }
        }
    }
} 