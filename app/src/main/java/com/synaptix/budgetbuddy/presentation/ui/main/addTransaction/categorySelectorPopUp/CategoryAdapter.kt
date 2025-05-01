package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.categorySelectorPopUp

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category

class CategoryAdapter(
    private val expenses: List<Category>,
    //private val incomes: List<Category>
    private val onExpenseClick: (Category) -> Unit,
    //private val onIncomeClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return CategoryViewHolder(view)
    }

    override  fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)

        fun bind(category: Category) {
            categoryName.text = category.categoryName

            // Convert icon name (string) to drawable resource ID
            val context = itemView.context
            val iconResId = context.resources.getIdentifier(category.categoryIcon, "drawable", context.packageName)
            if (iconResId != 0) {
                categoryIcon.setImageResource(iconResId)
            } else {
                categoryIcon.setImageResource(R.drawable.ic_circle_24) // fallback icon
            }

            // Parse the color string (e.g. "#FF5733") to a color int
            try {
                val colorInt = Color.parseColor(category.categoryColor)
                categoryIcon.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN)
            } catch (e: IllegalArgumentException) {
                // Fallback color if parsing fails
                categoryIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
            }

            itemView.setOnClickListener {
                onExpenseClick(category)
            }
        }
    }
}