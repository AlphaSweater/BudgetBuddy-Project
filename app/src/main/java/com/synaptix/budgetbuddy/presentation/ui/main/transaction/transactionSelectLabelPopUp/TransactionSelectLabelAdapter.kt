package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying a list of labels in a RecyclerView.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses a dedicated ViewHolder class
 * 3. Handles label selection through a callback
 * 4. Uses the Label's built-in isSelected field for selection state
 *
 * @param onSelectionChanged Callback function that is triggered when label selection changes
 */
class TransactionSelectLabelAdapter(
    private val onSelectionChanged: (List<Label>) -> Unit
) : BaseAdapter<Label, TransactionSelectLabelAdapter.LabelViewHolder>() {

    /**
     * Creates a new ViewHolder instance for label items.
     * Uses the standard item_label layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_label
        ) { LabelViewHolder(it) }
    }

    /**
     * Updates the adapter's data with a new list and selection state
     * @param newLabels The new list of labels
     * @param selectedLabels The list of currently selected labels
     */
    fun submitList(newLabels: List<Label>, selectedLabels: List<Label>) {
        val selectedIds = selectedLabels.map { it.id }.toSet()
        val updatedLabels = newLabels.map { label ->
            label.copy(isSelected = selectedIds.contains(label.id))
        }
        super.submitList(updatedLabels)
    }

    /**
     * Returns the list of currently selected labels
     */
    fun getSelectedLabels(): List<Label> {
        return items.filter { it.isSelected }
    }

    /**
     * Updates the selection state of a label and notifies listeners
     * @param label The label to update
     * @param isSelected The new selection state
     */
    private fun updateLabelSelection(label: Label, isSelected: Boolean) {
        val updatedLabels = items.map { 
            if (it.id == label.id) it.copy(isSelected = isSelected) else it 
        }
        submitList(updatedLabels)
        onSelectionChanged(getSelectedLabels())
    }

    /**
     * ViewHolder class for label items.
     * Responsible for binding label data to the view and handling selection events.
     */
    inner class LabelViewHolder(itemView: View) : BaseViewHolder<Label>(itemView) {
        private val labelTitle: TextView = itemView.findViewById(R.id.textLabelTitle)
        private val labelDescription: TextView = itemView.findViewById(R.id.textLabelDescription)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkSelect)

        /**
         * Binds label data to the view.
         * Sets the label name, description, and selection state.
         * Handles click events for both the item and checkbox.
         */
        override fun bind(item: Label) {
            labelTitle.text = item.name
            labelDescription.text = "label.transactionInfo"
            checkBox.isChecked = item.isSelected

            itemView.setOnClickListener {
                updateLabelSelection(item, !item.isSelected)
            }

            checkBox.setOnClickListener {
                updateLabelSelection(item, checkBox.isChecked)
            }
        }
    }
}