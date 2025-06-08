package com.synaptix.budgetbuddy.presentation.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
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
     */
    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Returns the current list of items
     * @return The current list of items
     */
    fun getCurrentList(): List<T> {
        return items
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
     * Base ViewHolder class that enforces a bind method
     */
    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }
} 