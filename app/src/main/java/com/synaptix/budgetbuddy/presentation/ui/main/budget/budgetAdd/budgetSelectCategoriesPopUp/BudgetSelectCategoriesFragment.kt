package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectCategoriesPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.R

// TODO: Make this a fragment pop up!!
class BudgetSelectCategoriesFragment : BottomSheetDialogFragment() {

    private lateinit var checkBoxSelectAll: CheckBox
    private lateinit var categoryCheckboxes: List<CheckBox>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget_select_categories, container, false)

        // Initialize checkboxes
        checkBoxSelectAll = view.findViewById(R.id.checkBoxSelectAll)

        categoryCheckboxes = listOf(
            view.findViewById(R.id.checkBoxBeauty),
            view.findViewById(R.id.checkBoxBillsAndFees),
            view.findViewById(R.id.checkBoxCar),
            view.findViewById(R.id.checkEducation),
            view.findViewById(R.id.checkEntertainment),
            view.findViewById(R.id.checkFamilyAndPerson),
            view.findViewById(R.id.checkFoodAndDrinks),
            view.findViewById(R.id.checkGifts),
            view.findViewById(R.id.checkGroceries),
            view.findViewById(R.id.checkHealthCare)
            // Add more if there are more checkboxes in your XML
        )

        // Handle Select All checkbox
        checkBoxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            categoryCheckboxes.forEach { it.isChecked = isChecked }
        }

        // Update Select All checkbox if any category is manually changed
        categoryCheckboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                checkBoxSelectAll.setOnCheckedChangeListener(null) // temporarily remove listener
                checkBoxSelectAll.isChecked = categoryCheckboxes.all { it.isChecked }
                checkBoxSelectAll.setOnCheckedChangeListener { _, isChecked ->
                    categoryCheckboxes.forEach { it.isChecked = isChecked }
                }
            }
        }

        // Optional: handle back button
        view.findViewById<ImageButton>(R.id.btnGoBack).setOnClickListener {
            dismiss()
        }

        return view
    }
}