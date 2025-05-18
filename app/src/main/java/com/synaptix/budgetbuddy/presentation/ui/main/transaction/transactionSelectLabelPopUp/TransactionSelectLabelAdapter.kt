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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_label
        ) { LabelViewHolder(it) }
    }

    fun getSelectedLabels(): List<Label> = items.filter { it.isSelected }

    inner class LabelViewHolder(view: View) : BaseViewHolder<Label>(view) {
        private val labelTitle: TextView = view.findViewById(R.id.textLabelTitle)
        private val labelDescription: TextView = view.findViewById(R.id.textLabelDescription)
        private val checkBox: CheckBox = view.findViewById(R.id.checkSelect)

        override fun bind(item: Label) {
            labelTitle.text = item.labelName
            labelDescription.text = item.transactionInfo
            checkBox.isChecked = item.isSelected

            itemView.setOnClickListener {
                item.isSelected = !item.isSelected
                checkBox.isChecked = item.isSelected
                onSelectionChanged(items.filter { it.isSelected })
            }

            checkBox.setOnClickListener {
                item.isSelected = checkBox.isChecked
                onSelectionChanged(items.filter { it.isSelected })
            }
        }
    }
}