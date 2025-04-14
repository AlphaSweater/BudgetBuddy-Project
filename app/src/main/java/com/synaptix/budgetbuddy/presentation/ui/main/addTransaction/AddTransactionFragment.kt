package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.synaptix.budgetbuddy.databinding.FragmentAddTransactionBinding

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addTransactionViewModel =
            ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textAddTransaction
//        addTransactionViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}