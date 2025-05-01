package com.synaptix.budgetbuddy.presentation.ui.main.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet

class WalletMainAdapter(
    private var walletList: List<Wallet> = emptyList()
) : RecyclerView.Adapter<WalletMainAdapter.WalletViewHolder>() {

    // ViewHolder class
    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val walletIcon: ImageView = itemView.findViewById(R.id.ivWalletIcon)
        val walletName: TextView = itemView.findViewById(R.id.tvWalletName)
        val walletCurrency: TextView = itemView.findViewById(R.id.tvCurrency)
        val walletAmount: TextView = itemView.findViewById(R.id.tvWalletAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_main, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = walletList[position]

        // Bind data
        holder.walletIcon.setImageResource(wallet.iconRes)
        holder.walletName.text = wallet.name
        holder.walletCurrency.text = wallet.currency
        holder.walletAmount.text = wallet.amount

        // Optional: handle click
        holder.itemView.setOnClickListener {
            // e.g., Toast.makeText(it.context, "Clicked: ${wallet.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return walletList.size
    }
}
