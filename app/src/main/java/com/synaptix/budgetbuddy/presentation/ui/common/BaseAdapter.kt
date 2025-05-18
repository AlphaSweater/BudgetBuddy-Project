package com.synaptix.budgetbuddy.presentation.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Base adapter class that implements common RecyclerView adapter patterns
 * @param T The type of data items in the list
 * @param VH The ViewHolder type
 */
abstract class BaseAdapter<T, VH : BaseAdapter.BaseViewHolder<T>> : RecyclerView.Adapter<VH>() {

    protected var items: List<T> = emptyList()
        private set

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Updates the adapter's data with a new list
     * @param newItems The new list of items
     * @param enableDiffUtil Whether to use DiffUtil for calculating changes (default: false)
     */
    fun submitList(newItems: List<T>, enableDiffUtil: Boolean = false) {
        if (enableDiffUtil) {
            val diffCallback = object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = items.size
                override fun getNewListSize(): Int = newItems.size
                override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean = 
                    areItemsSame(items[oldPos], newItems[newPos])
                override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean = 
                    areContentsSame(items[oldPos], newItems[newPos])
            }
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            items = newItems
            diffResult.dispatchUpdatesTo(this)
        } else {
            items = newItems
            notifyDataSetChanged()
        }
    }

    /**
     * Creates a ViewHolder for the adapter
     * @param parent The parent ViewGroup
     * @param layoutResId The layout resource ID for the item view
     * @return A new ViewHolder instance
     */
    protected fun createViewHolder(
        parent: ViewGroup,
        @LayoutRes layoutResId: Int,
        bindView: (View) -> VH
    ): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return bindView(view)
    }

    /**
     * Compare if two items represent the same object (e.g., same ID)
     * Override this if using DiffUtil
     */
    protected open fun areItemsSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

    /**
     * Compare if two items have the same content
     * Override this if using DiffUtil and need custom content comparison
     */
    protected open fun areContentsSame(oldItem: T, newItem: T): Boolean = oldItem == newItem

    /**
     * Base ViewHolder class that enforces a bind method
     */
    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }
} 