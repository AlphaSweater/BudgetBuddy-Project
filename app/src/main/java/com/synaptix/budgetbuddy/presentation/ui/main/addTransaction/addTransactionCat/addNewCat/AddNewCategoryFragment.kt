package com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat.addNewCat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentAddNewCategoryBinding
import com.synaptix.budgetbuddy.presentation.ui.main.addTransaction.addTransactionCat.addNewCat.AddNewCategoryViewModel

class AddNewCategoryFragment : Fragment() {

    private var _binding: FragmentAddNewCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddNewCategoryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListeners()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupOnClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}