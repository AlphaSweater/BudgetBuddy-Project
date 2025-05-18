package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

class TransactionSelectCategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
) : BaseAdapter<Category, TransactionSelectCategoryAdapter.CategoryViewHolder>() {

    private var originalItems = emptyList<Category>()
    private var filteredItems = emptyList<Category>()
    private var currentQuery = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return createViewHolder(
            parent = parent,
            layoutResId = R.layout.item_category
        ) { CategoryViewHolder(it) }
    }

    fun submitList(list: List<Category>) {
        originalItems = list
        filter(currentQuery) // Reapply current filter
    }

    fun filter(query: String) {
        currentQuery = query
        filteredItems = if (query.isEmpty()) {
            originalItems
        } else {
            originalItems.filter {
                it.categoryName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : BaseViewHolder<Category>(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)

        override fun bind(item: Category) {
            val context = itemView.context

            categoryName.text = item.categoryName
            categoryIcon.setImageResource(item.categoryIcon)
            val colorInt = ContextCompat.getColor(context, item.categoryColor)
            categoryIcon.setColorFilter(colorInt)

            itemView.setOnClickListener {
                onCategoryClick(item)
            }
        }
    }

    override fun getItemCount(): Int = filteredItems.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(filteredItems[position])
    }
}