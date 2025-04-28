package com.synaptix.budgetbuddy.ui.recurrence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.synaptix.budgetbuddy.databinding.FragmentRecurrenceBottomSheetBinding

class RecurrenceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentRecurrenceBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

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

        binding.optionEveryWeek2.setOnClickListener{
            // Handles "Every 2 weeks" click
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}