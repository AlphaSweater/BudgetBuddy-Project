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

package com.synaptix.budgetbuddy.presentation.ui.main.budget

import android.text.style.ForegroundColorSpan
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Budget
import com.synaptix.budgetbuddy.core.model.BudgetListItems
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel.ValidationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying the main budget list in the budget screen.
 * This adapter follows the standard pattern for RecyclerView adapters in the app:
 * 1. Extends BaseAdapter for common functionality
 * 2. Uses a dedicated ViewHolder class
 * 3. Handles item click events through a callback
 * 4. Displays budget icon, name, and status
 *
 * @param onBudgetClick Callback function that is triggered when a budget item is clicked
 */
class BudgetMainAdapter(
    private val onBudgetClick: (Budget) -> Unit
) : BaseAdapter<BudgetListItems.BudgetBudgetItem, BudgetMainAdapter.BudgetViewHolder>() {

    private val currencyFormat = NumberFormat.getNumberInstance(Locale("en", "ZA")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    /**
     * Creates a new ViewHolder instance for budget items.
     * Uses the standard item_current_budget layout resource.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        return createViewHolder(parent, R.layout.item_current_budget) { view ->
            BudgetViewHolder(view, onBudgetClick)
        }
    }

    /**
     * ViewHolder class for budget items in the main budget screen.
     * Responsible for binding budget data to the view and handling click events.
     * Displays:
     * - Budget icon
     * - Budget name
     * - Budget status
     */
    class BudgetViewHolder(
        itemView: View,
        private val onBudgetClick: (Budget) -> Unit
    ) : BaseViewHolder<BudgetListItems.BudgetBudgetItem>(itemView) {

        private val budgetIcon: ImageView = itemView.findViewById(R.id.budgetIcon)
        private val budgetTitle: TextView = itemView.findViewById(R.id.budgetTitle)
        private val budgetStatus: TextView = itemView.findViewById(R.id.budgetStatus)
        private val budgetProgress: LinearProgressIndicator = itemView.findViewById(R.id.budgetProgress)

        /**
         * Binds budget data to the view.
         * Sets the budget icon, name, and status.
         * Attaches click listener to the entire item view.
         */
        override fun bind(item: BudgetListItems.BudgetBudgetItem) {
            budgetIcon.setImageResource(R.drawable.ic_ui_budget)
            budgetTitle.text = item.budget.name

            val context = itemView.context
            val spentAmount = item.spentAmount
            val budgetedAmount = item.budgetedAmount
            val remainingAmount = item.remainingAmount

            // --- Setup status text with color ---
            val statusText = SpannableStringBuilder()

            val spentStr = "R ${currencyFormat.format(spentAmount)}"
            val spentStart = statusText.length
            statusText.append(spentStr)
            statusText.setSpan(
                ForegroundColorSpan(context.getColor(R.color.expense_red)),
                spentStart,
                spentStart + spentStr.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            statusText.append(" spent of ")

            val amountStr = "R ${currencyFormat.format(budgetedAmount)}"
            val amountStart = statusText.length
            statusText.append(amountStr)
            statusText.setSpan(
                ForegroundColorSpan(context.getColor(R.color.profit_green)),
                amountStart,
                amountStart + amountStr.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            budgetStatus.setText(statusText, TextView.BufferType.SPANNABLE)

            // --- Set progress ---
            val progress = if (budgetedAmount > 0) {
                ((spentAmount / budgetedAmount) * 100).coerceIn(0.0, 100.0)
            } else {
                0.0
            }
            budgetProgress.progress = progress.toInt()

            itemView.setOnClickListener { onBudgetClick(item.budget) }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private val _amount = MutableStateFlow<Double?>(null)
    val amount: StateFlow<Double?> = _amount

    fun setAmount(amount: Double?) {
        _amount.value = amount
        validateForm()
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Validation State
    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState
    // Validate the form fields and update validation state
    fun validateForm(): Boolean {
        val currentState = _validationState.value
        val isAmountValid = (_amount.value ?: 0.0) > 0.0

        _validationState.value = currentState.copy(
            isAmountValid = isAmountValid,
            amountError = if (currentState.shouldShowErrors && !isAmountValid) "Please enter a valid amount" else null,
        )

        return isAmountValid
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\

    companion object {
        private val currencyFormat = NumberFormat.getNumberInstance(Locale("en", "ZA")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\