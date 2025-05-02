package com.synaptix.budgetbuddy.presentation.ui.main.general.generalReports

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.synaptix.budgetbuddy.databinding.FragmentGeneralReportsBinding

class GeneralReportsFragment : Fragment() {

    private var _binding: FragmentGeneralReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralReportsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoBack.setOnClickListener { findNavController().popBackStack() }
    }
}