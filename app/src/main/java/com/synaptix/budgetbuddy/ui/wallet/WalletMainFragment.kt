package com.synaptix.budgetbuddy.ui.wallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAddWalletBinding
import com.synaptix.budgetbuddy.databinding.FragmentWalletMainBinding
import com.synaptix.budgetbuddy.presentation.ui.auth.AuthActivity

class WalletMainFragment : Fragment() {

    companion object {
        fun newInstance() = WalletMainFragment()
    }
    private var _binding: FragmentWalletMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.setOnClickListener{
            (activity as? AuthActivity)?.showLogin()
        }
    }
}