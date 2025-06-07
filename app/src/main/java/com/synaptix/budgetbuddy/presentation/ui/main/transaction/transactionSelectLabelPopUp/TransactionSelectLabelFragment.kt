package com.synaptix.budgetbuddy.presentation.ui.main.transaction.transactionSelectLabelPopUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectLabelBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import com.synaptix.budgetbuddy.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionSelectLabelFragment : Fragment() {

    private var _binding: FragmentTransactionSelectLabelBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: TransactionAddViewModel by navGraphViewModels(R.id.ind_transaction_navigation_graph) {defaultViewModelProviderFactory}
    private val labelViewModel: TransactionSelectLabelViewModel by viewModels()

    private val labelAdapter by lazy {
        TransactionSelectLabelAdapter(
            onSelectionChanged = { selectedLabels ->
                labelViewModel.updateSelectedLabels(selectedLabels)
            },
            onCreateNewLabel = { searchQuery ->
                // TODO: Implement create new label functionality
                // This will be implemented by you
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSelectLabelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        observeLabels()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewLabels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = labelAdapter
        }

        // Initialize with selected labels from ViewModel
        val initialSelected = sharedViewModel.selectedLabels.value ?: emptyList()
        labelAdapter.submitList(emptyList(), initialSelected, showCreateNew = true)
    }

    private fun setupClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            sharedViewModel.setLabels(labelAdapter.getSelectedLabels())
            findNavController().popBackStack()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            val query = text?.toString() ?: ""
            labelViewModel.filterLabels(query)
            binding.noLabelsContainer.visibility = View.GONE
        }
    }

    private fun observeLabels() {
        viewLifecycleOwner.lifecycleScope.launch {
            labelViewModel.loadLabelsForUser()
            labelViewModel.filteredLabels.collectLatest { labels ->
                val selectedLabels = sharedViewModel.selectedLabels.value ?: emptyList()
                labelAdapter.submitList(labels, selectedLabels, showCreateNew = true)
                
                // Show/hide no labels state
                binding.noLabelsContainer.visibility = if (labels.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewLabels.visibility = if (labels.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
