package com.synaptix.budgetbuddy.ui.recurrence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.databinding.FragmentRecurrenceBottomSheetBinding

class RecurrenceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentRecurrenceBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecurrenceBottomSheetBinding.inflate(inflater, container, false)

        binding.optionEveryDay.setOnClickListener {
            // Handle "Every day" click
            dismiss()
        }

        binding.optionEveryWeek.setOnClickListener {
            // Handle "Every week" click
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}