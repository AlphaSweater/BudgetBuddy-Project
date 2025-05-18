//======================================================================================
//Group 2 - Group Members:
//======================================================================================
//* Chad Fairlie ST10269509
//* Dhiren Ruthenavelu ST10256859
//* Kayla Ferreira ST10259527
//* Nathan Teixeira ST10249266
//======================================================================================
//Declaration:
//======================================================================================
//We declare that this work is our own original work and that no part of it has been
//copied from any other source, except where explicitly acknowledged.
//======================================================================================
//References:
//======================================================================================
//* ChatGPT was used to help with the design and planning. As well as assisted with
//finding and fixing errors in the code.
//* ChatGPT also helped with the forming of comments for the code.
//* https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
//======================================================================================

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectCategoryPopUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category

// Adapter class for displaying a list of categories in RecyclerView
class BudgetSelectCategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit // Lambda to handle category click
) : RecyclerView.Adapter<BudgetSelectCategoryAdapter.CategoryViewHolder>() {

    // Inflates the item layout and creates the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // Binds data to the ViewHolder
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    // Returns total count of categories
    override fun getItemCount(): Int = categories.size

    // ViewHolder class to hold references to views in each item
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.txtCategoryName)

        // Binds a Category object to the item layout
        fun bind(category: Category) {
            val context = itemView.context

            // Set category name and icon
            categoryName.text = category.categoryName
            categoryIcon.setImageResource(category.categoryIcon)

            // Set color filter for icon based on category color
            val colorInt = ContextCompat.getColor(context, category.categoryColor)
            categoryIcon.setColorFilter(colorInt)

            // Handle item click event
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}
