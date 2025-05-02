package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.BudgetReportListItems
import com.synaptix.budgetbuddy.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeMainFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeMainViewModel by activityViewModels()

    private lateinit var homeAdapter: HomeAdapter

    private val TAG = "HomeMainFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflating layout")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view created")

        viewModel.loadWallets()
        Log.d(TAG, "onViewCreated: Requested to load wallets")

        viewModel.loadTransactions()
        Log.d(TAG, "onViewCreated: Requested to load transactions")

        viewModel.loadCategories()
        Log.d(TAG, "onViewCreated: Requested to load categories")

        viewModel.wallets.observe(viewLifecycleOwner) { walletList ->
            Log.d(TAG, "wallets.observe: Received ${walletList.size} wallets")

            val homeWalletItems = walletList.map { wallet ->
                Log.d(TAG, "Mapping wallet: ${wallet.walletName}, Balance: ${wallet.walletBalance}")
                BudgetReportListItems.HomeWalletItem(
                    walletName = wallet.walletName,
                    walletIcon = R.drawable.baseline_shopping_bag_24,
                    walletBalance = wallet.walletBalance,
                    relativeDate = "Recent"
                )
            }

            setupWalletRecycler(homeWalletItems)
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactionList ->
            Log.d(TAG, "transactions.observe: Received ${transactionList.size} transactions")

            val transactionItems = transactionList.map { transaction ->
                Log.d(TAG, "Mapping transaction: ${transaction.transactionId}, Amount: ${transaction.amount}")
                BudgetReportListItems.TransactionItem(
                    categoryName = transaction.category!!.categoryName,
                    categoryIcon = transaction.category.categoryIcon,
                    categoryColour = transaction.category.categoryColor,
                    amount = transaction.amount,
                    walletName = transaction.wallet!!.walletName,
                    note = transaction.note,
                    relativeDate = transaction.date
                )
            }

            setupTransactionRecycler(transactionItems)
        }

        viewModel.categories.observe(viewLifecycleOwner) { categoryList ->
            Log.d(TAG, "categories.observe: Received ${categoryList.size} categories")

            val categoryItems = categoryList.map { category ->
                Log.d(TAG, "Mapping category: ${category.categoryName}, Icon: ${category.categoryIcon}")
                BudgetReportListItems.CategoryItems(
                    categoryName = category.categoryName,
                    categoryIcon = category.categoryIcon,
                    categoryColour = category.categoryColor,
                    transactionCount = 0, // Placeholder for transaction count
                    amount = "0.00", // Placeholder for amount
                    relativeDate = "Recent" // Placeholder for relative date
                )
            }

            setupCategoryRecycler(categoryItems)
        }
    }

    private fun setupWalletRecycler(walletList: List<BudgetReportListItems.HomeWalletItem>) {
        Log.d(TAG, "Setting up RecyclerView with ${walletList.size} items")
        binding.recyclerViewHomeWalletOverview.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter(walletList)
        binding.recyclerViewHomeWalletOverview.adapter = homeAdapter
    }

    private fun setupTransactionRecycler(transactionList: List<BudgetReportListItems.TransactionItem>) {
        Log.d(TAG, "Setting up RecyclerView with ${transactionList.size} items")
        binding.recyclerViewHomeTransactionOverview.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter(transactionList)
        binding.recyclerViewHomeTransactionOverview.adapter = homeAdapter
    }

    private fun setupCategoryRecycler(categoryList: List<BudgetReportListItems.CategoryItems>) {
        Log.d(TAG, "Setting up RecyclerView with ${categoryList.size} items")
        binding.recyclerViewHomeCategoryOverview.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter(categoryList)
        binding.recyclerViewHomeCategoryOverview.adapter = homeAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up binding")
        _binding = null
    }
}
