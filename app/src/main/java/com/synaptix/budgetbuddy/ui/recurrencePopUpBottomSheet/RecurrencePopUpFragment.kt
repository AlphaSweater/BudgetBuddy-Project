package com.synaptix.budgetbuddy.ui.recurrencePopUpBottomSheet

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synaptix.budgetbuddy.R

class RecurrencePopUpFragment : Fragment() {


    companion object {
        fun newInstance() = RecurrencePopUpFragment()
    }

    private val viewModel: RecurrencePopUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recurrence_pop_up, container, false)
    }
}