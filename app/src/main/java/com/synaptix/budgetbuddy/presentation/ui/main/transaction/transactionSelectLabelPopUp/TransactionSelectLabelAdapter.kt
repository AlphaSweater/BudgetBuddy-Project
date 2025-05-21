package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

class TransactionSelectLabelAdapter(
    private val onSelectionChanged: (List<Label>) -> Unit
) : BaseAdapter<Label, TransactionSelectLabelAdapter.LabelViewHolder>() {

    private val selectedLabels = mutableSetOf<Label>()

    fun submitList(newLabels: List<Label>, initialSelected: List<Label> = emptyList()) {
        selectedLabels.clear()
        selectedLabels.addAll(initialSelected)
        super.submitList(newLabels)
    }

    fun getSelectedLabels(): List<Label> = selectedLabels.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_label
        ) { LabelViewHolder(it) }
    }

    inner class LabelViewHolder(view: View) : BaseViewHolder<Label>(view) {
        private val labelTitle: TextView = view.findViewById(R.id.textLabelTitle)
        private val labelDescription: TextView = view.findViewById(R.id.textLabelDescription)
        private val checkBox: CheckBox = view.findViewById(R.id.checkSelect)

        override fun bind(item: Label) {
            labelTitle.text = item.name
            labelDescription.text = "0 transactions in 0 wallets" // TODO: Get actual transaction info
            checkBox.isChecked = selectedLabels.contains(item)

            itemView.setOnClickListener {
                toggleLabelSelection(item)
            }

            checkBox.setOnClickListener {
                toggleLabelSelection(item)
            }
        }

        private fun toggleLabelSelection(label: Label) {
            if (selectedLabels.contains(label)) {
                selectedLabels.remove(label)
            } else {
                selectedLabels.add(label)
            }
            checkBox.isChecked = selectedLabels.contains(label)
            onSelectionChanged(selectedLabels.toList())
        }
    }
}