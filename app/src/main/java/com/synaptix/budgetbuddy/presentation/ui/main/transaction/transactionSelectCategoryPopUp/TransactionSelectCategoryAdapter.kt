package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectCategoryPopUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.CategoryIn

class TransactionSelectCategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<TransactionSelectCategoryAdapter.CategoryViewHolder>() {

    private var filteredCategories = categories.toList()
    val currentList: List<Category> get() = filteredCategories

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(filteredCategories[position])
    }

    override fun getItemCount(): Int = filteredCategories.size

    fun filter(query: String) {
        filteredCategories = if (query.isEmpty()) {
            categories
        } else {
            categories.filter {
                it.categoryName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)

        fun bind(category: Category) {
            val context = itemView.context

            categoryName.text = category.categoryName
            categoryIcon.setImageResource(category.categoryIcon)
            val colorInt = ContextCompat.getColor(context, category.categoryColor)
            categoryIcon.setColorFilter(colorInt)

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}