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
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.databinding.FragmentTransactionSelectLabelBinding
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel
import com.synaptix.budgetbuddy.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionSelectLabelFragment : Fragment() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Properties
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private var _binding: FragmentTransactionSelectLabelBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: TransactionAddViewModel by navGraphViewModels(R.id.ind_transaction_navigation_graph) {defaultViewModelProviderFactory}
    private val labelViewModel: TransactionSelectLabelViewModel by viewModels()

    private val labelAdapter by lazy {
        TransactionSelectLabelAdapter(
            onSelectionChanged = { selectedLabels ->
                labelViewModel.updateSelectedLabels(selectedLabels)
            }
        )
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Fragment Lifecycle
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
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
        setupViews()
        observeLabels()
        observeLoadingState()
        observeErrorState()
        labelViewModel.loadLabelsForUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initial Setup Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewLabels.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = labelAdapter
        }

        // Initialize with selected labels from ViewModel
        val initialSelected = sharedViewModel.selectedLabels.value ?: emptyList()
        labelAdapter.submitList(emptyList(), initialSelected)
    }

    private fun setupClickListeners() {
        binding.btnGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            sharedViewModel.setLabels(labelAdapter.getSelectedLabels())
            findNavController().popBackStack()
        }

        binding.createNewLabelContainer.setOnClickListener {
            val searchQuery = binding.searchEditText.text?.toString() ?: ""
            handleCreateNewLabel(searchQuery)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            labelViewModel.filterLabels(text?.toString() ?: "")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Observation Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun observeLabels() {
        viewLifecycleOwner.lifecycleScope.launch {
            labelViewModel.filteredLabels.collectLatest { labels ->
                labelAdapter.submitList(labels, labelAdapter.getSelectedLabels())
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            labelViewModel.isLoading.collectLatest { isLoading ->
                binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.recyclerViewLabels.visibility = if (isLoading) View.GONE else View.VISIBLE
                binding.searchLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
                binding.createNewLabelContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

    private fun observeErrorState() {
        viewLifecycleOwner.lifecycleScope.launch {
            labelViewModel.error.collectLatest { error ->
                error?.let {
                    showError(it)
                    labelViewModel.clearError()
                }
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Label Creation Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun handleCreateNewLabel(name: String) {
        if (labelViewModel.validateLabelName(name)) {
            labelViewModel.createNewLabel(name)
            // Clear the search field after creating
            binding.searchEditText.setText("")
        } else {
            showError("Invalid label name. Please try again.")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
