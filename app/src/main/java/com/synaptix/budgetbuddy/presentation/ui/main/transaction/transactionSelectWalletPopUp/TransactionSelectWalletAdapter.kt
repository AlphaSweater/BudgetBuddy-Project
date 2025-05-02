package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectWalletPopUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet

class TransactionSelectWalletAdapter(
    private val wallets: List<Wallet>,
    private val onWalletClick: (Int) -> Unit // Callback with selected wallet ID
) : RecyclerView.Adapter<TransactionSelectWalletAdapter.WalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(wallets[position])
    }

    override fun getItemCount(): Int = wallets.size

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.walletName)
        private val balance: TextView = itemView.findViewById(R.id.walletBalance)

        fun bind(wallet: Wallet) {
            name.text = wallet.walletName
            balance.text = "R %.2f".format(wallet.walletBalance)

            itemView.setOnClickListener {
                onWalletClick(wallet.walletId)

            }
        }
    }
}
