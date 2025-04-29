package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelectorDialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.synaptix.budgetbuddy.R

class LabelAdapter(
    private val labels: List<String>,
    private val preselected: Set<String>
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    private val selected = preselected.toMutableSet()

    inner class LabelViewHolder(val chip: Chip) : RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chip_label, parent, false) as Chip
        return LabelViewHolder(chip)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        val label = labels[position]
        holder.chip.text = label
        holder.chip.isChecked = selected.contains(label)

        holder.chip.setOnClickListener {
            if (selected.contains(label)) selected.remove(label)
            else selected.add(label)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = labels.size

    fun getSelectedLabels(): List<String> = selected.toList()
}