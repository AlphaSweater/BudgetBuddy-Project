package com.synaptix.budgetbuddy.presentation.ui.main.wallet.addWallet.ChangeWalletIcon

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class NewWalletCategoryFragment : Fragment() {

    companion object {
        fun newInstance() = NewWalletCategoryFragment()
    }

    private val viewModel: NewWalletCategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_new_wallet_category, container, false)
    }
}