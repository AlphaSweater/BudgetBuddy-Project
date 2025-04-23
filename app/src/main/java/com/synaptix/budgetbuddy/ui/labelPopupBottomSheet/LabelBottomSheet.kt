package com.synaptix.budgetbuddy.ui.labelPopupBottomSheet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class LabelBottomSheet : Fragment() {

    companion object {
        fun newInstance() = LabelBottomSheet()
    }

    private val viewModel: LabelBottomSheetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_label_bottom_sheet, container, false)
    }
}