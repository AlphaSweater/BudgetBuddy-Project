package com.synaptix.budgetbuddy.presentation.ui.main.wallet.walletAdd.walletSelectIconPopUp

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class WalletSelectIconFragment : Fragment() {

    companion object {
        fun newInstance() = WalletSelectIconFragment()
    }

    private val viewModel: WalletSelectIconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_wallet_select_icon, container, false)
    }
}