package com.synaptix.budgetbuddy.presentation.ui.main.wallet.reportWallet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.synaptix.budgetbuddy.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentWalletReportBinding

class WalletReportFragment : Fragment() {

    private var _binding: FragmentWalletReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletReportViewModel by viewModels()
    private lateinit var walletAdapter:WalletAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        walletAdapter = WalletAdapter()

        binding.recyclerViewWalletReport.apply {
            layoutManager = LinearLayoutManager(requireContext())
            walletAdapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}