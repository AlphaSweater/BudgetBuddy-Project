package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.labelSelector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import java.io.Serializable

class LabelAdapter(
    private val labels: List<Label>,
    private val onSelectionChanged: (List<Label>) -> Unit
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_label, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(labels[position])
    }

    override fun getItemCount(): Int = labels.size

    inner class LabelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val labelTitle: TextView = view.findViewById(R.id.textLabelTitle)
        private val labelDescription: TextView = view.findViewById(R.id.textLabelDescription)
        private val checkBox: CheckBox = view.findViewById(R.id.checkSelect)

        fun bind(label: Label) {
            labelTitle.text = label.labelName
            labelDescription.text = label.transactionInfo
            checkBox.isChecked = label.isSelected

            itemView.setOnClickListener {
                label.isSelected = !label.isSelected
                checkBox.isChecked = label.isSelected
                onSelectionChanged(labels.filter { it.isSelected })
            }

            checkBox.setOnClickListener {
                label.isSelected = checkBox.isChecked
                onSelectionChanged(labels.filter { it.isSelected })
            }
        }
    }

    fun getSelectedLabels(): List<Label> = labels.filter { it.isSelected }
}

data class Label(
    val labelName: String,
    val transactionInfo: String,
    var isSelected: Boolean = false
) : Serializable