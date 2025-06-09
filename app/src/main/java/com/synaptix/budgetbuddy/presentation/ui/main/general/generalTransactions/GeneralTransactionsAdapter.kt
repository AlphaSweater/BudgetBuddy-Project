package com.synaptix.budgetbuddy.presentation.ui.main.general.generalTransactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.databinding.ItemHomeTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying transaction items in the general transactions screen.
 * 
 * This adapter handles transaction items with their associated metadata.
 * Each transaction item shows:
 * - Category icon and name
 * - Wallet name
 * - Transaction amount (with color based on type)
 * - Optional note
 * - Relative date
 * 
 * The adapter uses view binding for efficient view access and follows the standard
 * pattern for RecyclerView adapters in the app.
 * 
 * @param onTransactionClick Callback for transaction item clicks
 */
class GeneralTransactionsAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<TransactionListItems.TransactionItem, GeneralTransactionsAdapter.TransactionViewHolder>(
    TransactionDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemHomeTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding, onTransactionClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position) as? TransactionListItems.TransactionItem
        item?.let { holder.bind(it) }
    }

    /**
     * ViewHolder for transaction items in the transactions screen.
     * Displays transaction details including category, wallet, amount, and date.
     * 
     * @param binding The view binding for the transaction item layout
     * @param onClick Callback for transaction item clicks
     */
    class TransactionViewHolder(
        private val binding: ItemHomeTransactionBinding,
        private val onClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds transaction data to the view.
         * Sets the category icon and name, wallet name, amount (with color),
         * optional note, and relative date.
         * 
         * @param item The TransactionListItems object containing transaction data
         */
        fun bind(item: TransactionListItems.TransactionItem) {
            val transaction = item.transaction
            val context = itemView.context

            // Set category icon and name
            binding.iconCategory.setImageResource(transaction.category.icon)
            binding.iconCategory.setColorFilter(
                ContextCompat.getColor(context, transaction.category.color)
            )
            binding.textCategoryName.text = transaction.category.name

            // Set wallet name
            binding.textWalletName.text = transaction.wallet.name

            val isIncome = transaction.category.type.equals("income", ignoreCase = true)
            val amountText = if (isIncome) {
                "+R ${String.format("%.2f", transaction.amount)}"
            } else {
                "-R ${String.format("%.2f", transaction.amount)}"
            }
            binding.textAmount.text = amountText
            binding.textAmount.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isIncome) R.color.profit_green else R.color.expense_red
                )
            )

            // Set note if available
            if (transaction.note.isNotEmpty()) {
                binding.textNote.text = transaction.note
                binding.rowNote.visibility = View.VISIBLE
            } else {
                binding.rowNote.visibility = View.GONE
            }

            // Set relative date
            binding.textDate.text = item.relativeDate

            // Set click listener
            itemView.setOnClickListener { onClick(transaction) }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     * Compares transaction items based on their IDs and content.
     */
    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionListItems.TransactionItem>() {
        override fun areItemsTheSame(
            oldItem: TransactionListItems.TransactionItem,
            newItem: TransactionListItems.TransactionItem
        ): Boolean {
            return oldItem.transaction.id == newItem.transaction.id
        }

        override fun areContentsTheSame(
            oldItem: TransactionListItems.TransactionItem,
            newItem: TransactionListItems.TransactionItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Sealed class representing different types of items that can be displayed
 * in the transactions list.
 */
sealed class TransactionListItems {
    /**
     * Represents a transaction item with its associated metadata.
     * 
     * @param transaction The transaction data
     * @param relativeDate The relative date string for display
     */
    data class TransactionItem(
        val transaction: Transaction,
        val relativeDate: String
    ) : TransactionListItems()
}