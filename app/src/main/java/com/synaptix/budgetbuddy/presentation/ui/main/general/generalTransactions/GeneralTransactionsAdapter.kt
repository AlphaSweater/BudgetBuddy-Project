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
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.databinding.ItemHomeTransactionBinding
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter
import com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports.ReportListItems
import java.text.SimpleDateFormat
import java.util.*

class GeneralTransactionsAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<ReportListItems.ReportTransactionItem, GeneralTransactionsAdapter.TransactionViewHolder>(
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
        val item = getItem(position) as? ReportListItems.ReportTransactionItem
        item?.let { holder.bind(it) }
    }

    class TransactionViewHolder(
        private val binding: ItemHomeTransactionBinding,
        private val onClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportListItems.ReportTransactionItem) {
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

    class TransactionDiffCallback : DiffUtil.ItemCallback<ReportListItems.ReportTransactionItem>() {
        override fun areItemsTheSame(
            oldItem: ReportListItems.ReportTransactionItem,
            newItem: ReportListItems.ReportTransactionItem
        ): Boolean {
            return oldItem.transaction.id == newItem.transaction.id
        }

        override fun areContentsTheSame(
            oldItem: ReportListItems.ReportTransactionItem,
            newItem: ReportListItems.ReportTransactionItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}