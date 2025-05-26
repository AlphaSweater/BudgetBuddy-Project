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

package com.synaptix.budgetbuddy.presentation.ui.main.budget.budgetAdd.budgetSelectWalletPopUp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Wallet

// Adapter for displaying a list of wallets in a RecyclerView
class BudgetSelectWalletAdapter(
    private var wallets: List<Wallet>,
    private val onWalletClick: (Wallet) -> Unit // Callback when a wallet is selected
) : RecyclerView.Adapter<BudgetSelectWalletAdapter.WalletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_wallet, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(wallets[position])
    }

    override fun getItemCount(): Int = wallets.size

    // ViewHolder class for binding wallet data to the UI
    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.walletName)
        private val balance: TextView = itemView.findViewById(R.id.walletBalance)

        fun bind(wallet: Wallet) {
            name.text = wallet.name
            balance.text = "R %.2f".format(wallet.balance)

            itemView.setOnClickListener {
                onWalletClick(wallet)
            }
        }
    }

    // Function to update the list of wallets and refresh the RecyclerView
    fun updateData(newList: List<Wallet>) {
        wallets = newList
        notifyDataSetChanged()
    }
}
