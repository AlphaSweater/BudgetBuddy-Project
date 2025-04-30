package com.synaptix.budgetbuddy.ui.wallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAddWalletBinding

class AddWalletFragment : Fragment() {

    private var _binding: FragmentAddWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddWalletViewModel by viewModels()


    companion object {
        fun newInstance() = AddWalletFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //oncliick listener that saves user input and injects into usecase
        binding.btnSave.setOnClickListener(
            val name = binding.edtWalletName.text.toString()
            val currency = binding.edtCurrency.text.toString()
            val balance = binding.edtInitialAmount.text.toString()

            if (name.isEmpty()) {
                binding.edtWalletName.error = "Wallet name is required"
                return@setOnClickListener
            }

            if (currency.isEmpty()) {
                binding.edtCurrency.error = "Currency is required"
                return@setOnClickListener
            }

            if (balance.isEmpty()) {
                binding.edtInitialAmount.error = "Initial amount is required"
                return@setOnClickListener
            }

            viewModel.addWallet(name, currency, balance)
        )
    }

}