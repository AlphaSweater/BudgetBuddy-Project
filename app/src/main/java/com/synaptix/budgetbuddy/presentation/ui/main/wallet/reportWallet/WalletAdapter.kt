package com.synaptix.budgetbuddy.presentation.ui.main.wallet.reportWallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet

class WalletAdapter(
    private var wallets: List<Wallet> = emptyList()
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet, parent, false)  // This assumes you have item_wallet.xml
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(wallets[position])
    }

    override fun getItemCount(): Int = wallets.size

    fun updateWallets(newWallets: List<Wallet>) {
        wallets = newWallets
        notifyDataSetChanged()
    }

    inner class WalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val transactionNameText: TextView = view.findViewById(R.id.tvTransactionName)
        private val transactionCategoryText: TextView = view.findViewById(R.id.tvTransactionCategory)
        private val walletText: TextView = view.findViewById(R.id.tvWallet)
        private val transactionAmountText: TextView = view.findViewById(R.id.tvTransactionAmount)
        private val transactionDayText: TextView = view.findViewById(R.id.tvTransactionDay)

        fun bind(wallet: Wallet) {
            transactionNameText.text = wallet.walletName
            transactionCategoryText.text = wallet.category
            walletText.text = wallet.walletName
            transactionAmountText.text = wallet.amount.toString()
            transactionDayText.text = wallet.date
        }
    }
}
