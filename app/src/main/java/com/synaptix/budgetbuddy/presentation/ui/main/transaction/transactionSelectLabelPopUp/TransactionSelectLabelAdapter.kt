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
 * @param onCreateNewLabel Callback function that is triggered when the "Create new label" option is selected
 */
class TransactionSelectLabelAdapter(
    private val onSelectionChanged: (List<Label>) -> Unit,
    private val onCreateNewLabel: (String) -> Unit
) : BaseAdapter<LabelItem, TransactionSelectLabelAdapter.LabelViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LABEL = 0
        private const val VIEW_TYPE_CREATE_NEW = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is LabelItem.CreateNew -> VIEW_TYPE_CREATE_NEW
            is LabelItem.Label -> VIEW_TYPE_LABEL
        }
    }

    /**
     * Creates a new ViewHolder instance for label items.
     * Uses the standard item_label layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        return when (viewType) {
            VIEW_TYPE_CREATE_NEW -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_create_new_label
            ) { LabelViewHolder(it, viewType) }
            else -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_transaction_label
            ) { LabelViewHolder(it, viewType) }
        }
    }

    /**
     * Updates the adapter's data with a new list and selection state
     * @param newLabels The new list of labels
     * @param selectedLabels The list of currently selected labels
     * @param showCreateNew Whether to show the "Create new label" option
     */
    fun submitList(newLabels: List<Label>, selectedLabels: List<Label>, showCreateNew: Boolean = false) {
        val selectedIds = selectedLabels.map { it.id }.toSet()
        val updatedLabels = newLabels.map { label ->
            label.copy(isSelected = selectedIds.contains(label.id))
        }
        
        val items = mutableListOf<LabelItem>()
        if (showCreateNew && newLabels.isEmpty()) {
            items.add(LabelItem.CreateNew)
        }
        items.addAll(updatedLabels.map { LabelItem.Label(it) })
        
        super.submitList(items)
    }

    /**
     * Returns the list of currently selected labels
     */
    fun getSelectedLabels(): List<Label> {
        return items.filterIsInstance<LabelItem.Label>()
            .filter { it.label.isSelected }
            .map { it.label }
    }

    /**
     * Updates the selection state of a label and notifies listeners
     * @param label The label to update
     * @param isSelected The new selection state
     */
    private fun updateLabelSelection(label: Label, isSelected: Boolean) {
        val updatedLabels = items.map { item ->
            when (item) {
                is LabelItem.Label -> if (item.label.id == label.id) {
                    LabelItem.Label(item.label.copy(isSelected = isSelected))
                } else item
                is LabelItem.CreateNew -> item
            }
        }
        submitList(updatedLabels)
        onSelectionChanged(getSelectedLabels())
    }

    /**
     * ViewHolder class for label items.
     * Responsible for binding label data to the view and handling selection events.
     */
    inner class LabelViewHolder(itemView: View, private val viewType: Int) : BaseViewHolder<LabelItem>(itemView) {
        private val labelTitle: TextView? = itemView.findViewById(R.id.textLabelTitle)
        private val labelDescription: TextView? = itemView.findViewById(R.id.textLabelDescription)
        private val checkBox: CheckBox? = itemView.findViewById(R.id.checkSelect)
        private val createNewText: TextView? = itemView.findViewById(R.id.textCreateNew)

        /**
         * Binds label data to the view.
         * Sets the label name, description, and selection state.
         * Handles click events for both the item and checkbox.
         */
        override fun bind(item: LabelItem) {
            when (item) {
                is LabelItem.Label -> {
                    labelTitle?.text = item.label.name
                    labelDescription?.text = "label.transactionInfo"
                    checkBox?.isChecked = item.label.isSelected

                    itemView.setOnClickListener {
                        updateLabelSelection(item.label, !item.label.isSelected)
                    }

                    checkBox?.setOnClickListener {
                        updateLabelSelection(item.label, checkBox.isChecked)
                    }
                }
                is LabelItem.CreateNew -> {
                    createNewText?.text = "Create new label"
                    itemView.setOnClickListener {
                        onCreateNewLabel("")
                    }
                }
            }
        }
    }
}

sealed class LabelItem {
    data class Label(val label: com.synaptix.budgetbuddy.core.model.Label) : LabelItem()
    object CreateNew : LabelItem()
}