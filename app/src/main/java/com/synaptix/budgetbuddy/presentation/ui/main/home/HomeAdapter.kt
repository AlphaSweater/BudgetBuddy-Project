package com.synaptix.budgetbuddy.presentation.ui.main.home

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.HomeListItems
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import com.synaptix.budgetbuddy.core.util.DateUtil
import com.synaptix.budgetbuddy.presentation.ui.common.BaseAdapter

/**
 * Adapter for displaying a heterogeneous list of items in the home screen.
 * 
 * This adapter handles multiple view types:
 * 1. Wallet items - Shows wallet balance and recent activity
 * 2. Transaction items - Shows transaction details with category and wallet info
 * 3. Category items - Shows category summary with transaction count and amount
 * 
 * Each item type has its own ViewHolder and layout resource.
 * The adapter follows the standard pattern for RecyclerView adapters in the app.
 * 
 * @param onWalletClick Optional callback for wallet item clicks
 * @param onTransactionClick Optional callback for transaction item clicks
 * @param onCategoryClick Optional callback for category item clicks
 */
class HomeAdapter(
    private val onWalletClick: ((Wallet) -> Unit)? = null,
    private val onTransactionClick: ((Transaction) -> Unit)? = null,
    private val onCategoryClick: ((Category) -> Unit)? = null
) : BaseAdapter<HomeListItems, BaseAdapter.BaseViewHolder<HomeListItems>>() {

    companion object {
        private const val VIEW_TYPE_WALLET = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_CATEGORY = 2
    }

    /**
     * Determines the view type for the item at the given position.
     * This is used by the RecyclerView to create the appropriate ViewHolder.
     * 
     * @param position The position of the item in the list
     * @return An integer representing the view type
     */
    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomeListItems.HomeWalletItem -> VIEW_TYPE_WALLET
        is HomeListItems.HomeTransactionItem -> VIEW_TYPE_TRANSACTION
        is HomeListItems.HomeCategoryItem -> VIEW_TYPE_CATEGORY
    }

    /**
     * Creates the appropriate ViewHolder based on the view type.
     * Each view type has its own layout resource and ViewHolder implementation.
     * 
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<HomeListItems> {
        return when (viewType) {
            VIEW_TYPE_WALLET -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_wallet_main
            ) { WalletViewHolder(it, onWalletClick) }

            VIEW_TYPE_TRANSACTION -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_transaction
            ) { TransactionViewHolder(it, onTransactionClick) }

            VIEW_TYPE_CATEGORY -> createViewHolder(
                parent = parent,
                layoutResId = R.layout.item_home_category
            ) { CategoryViewHolder(it, onCategoryClick) }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    /**
     * ViewHolder for wallet items in the home screen.
     * Displays wallet name, balance, and relative date.
     * 
     * @param itemView The view for this ViewHolder
     * @param onClick Optional callback for wallet item clicks
     */
    class WalletViewHolder(
        itemView: View,
        private val onClick: ((Wallet) -> Unit)?
    ) : BaseViewHolder<HomeListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.walletIcon)
        private val nameText: TextView = itemView.findViewById(R.id.walletName)
        private val balanceText: TextView = itemView.findViewById(R.id.walletBalance)
        private val dateText: TextView = itemView.findViewById(R.id.lastActivity)

        /**
         * Binds wallet data to the view.
         * Sets the wallet icon, name, balance, and last activity date.
         * 
         * @param item The HomeListItems object containing wallet data
         */
        override fun bind(item: HomeListItems) {
            if (item !is HomeListItems.HomeWalletItem) return

            iconView.setImageResource(R.drawable.ic_ui_wallet)
            nameText.text = item.wallet.name
            balanceText.text = CurrencyUtil.formatWithSymbol(item.wallet.balance)
            dateText.text = "• ${DateUtil.formatDate(item.wallet.lastTransactionAt)}"

            itemView.setOnClickListener { onClick?.invoke(item.wallet) }
        }
    }

    /**
     * ViewHolder for transaction items in the home screen.
     * Displays category icon, name, amount, date, and wallet name.
     * Applies category color to the icon background.
     * 
     * @param itemView The view for this ViewHolder
     * @param onClick Optional callback for transaction item clicks
     */
    class TransactionViewHolder(
        itemView: View,
        private val onClick: ((Transaction) -> Unit)?
    ) : BaseViewHolder<HomeListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iconCategory)
        private val categoryName: TextView = itemView.findViewById(R.id.textCategoryName)
        private val walletIcon: ImageView = itemView.findViewById(R.id.iconWallet)
        private val walletName: TextView = itemView.findViewById(R.id.textWalletName)
        private val noteContainer: LinearLayout = itemView.findViewById(R.id.rowNote)
        private val noteText: TextView = itemView.findViewById(R.id.textNote)
        private val amountText: TextView = itemView.findViewById(R.id.textAmount)
        private val dateText: TextView = itemView.findViewById(R.id.textDate)

        /**
         * Binds transaction data to the view.
         * Sets the category icon and color, transaction details, and wallet info.
         * Handles optional note display and amount color based on transaction type.
         * 
         * @param item The HomeListItems object containing transaction data
         */
        override fun bind(item: HomeListItems) {
            if (item !is HomeListItems.HomeTransactionItem) return

            val resolvedColor = ContextCompat.getColor(itemView.context, item.transaction.category.color)

            iconView.setImageResource(item.transaction.category.icon)
            iconView.setColorFilter(resolvedColor)
            categoryName.text = item.transaction.category.name

            walletIcon.setImageResource(R.drawable.ic_ui_wallet)
            walletName.text = item.transaction.wallet.name

            if (item.transaction.note.isNotBlank()) {
                noteContainer.visibility = View.VISIBLE
                noteText.text = item.transaction.note
            } else {
                noteContainer.visibility = View.GONE
            }

            amountText.text = CurrencyUtil.formatWithSymbol(item.transaction.amount)

            val colorRes = if (item.transaction.category.type.equals("INCOME", ignoreCase = true)) {
                R.color.profit_green
            } else {
                R.color.expense_red
            }

            amountText.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            dateText.text = item.relativeDate

            itemView.setOnClickListener { onClick?.invoke(item.transaction) }
        }
    }

    /**
     * ViewHolder for category items in the home screen.
     * Displays category icon, name, transaction count, amount, and date.
     * Applies category color to the icon background.
     * 
     * @param itemView The view for this ViewHolder
     * @param onClick Optional callback for category item clicks
     */
    class CategoryViewHolder(
        itemView: View,
        private val onClick: ((Category) -> Unit)?
    ) : BaseViewHolder<HomeListItems>(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val nameText: TextView = itemView.findViewById(R.id.categoryName)
        private val transactionsText: TextView = itemView.findViewById(R.id.categoryTransactions)
        private val amountText: TextView = itemView.findViewById(R.id.categoryAmount)
        private val dateText: TextView = itemView.findViewById(R.id.categoryDate)

        /**
         * Binds category data to the view.
         * Sets the category icon and color, name, transaction count, amount, and date.
         * 
         * @param item The HomeListItems object containing category data
         */
        override fun bind(item: HomeListItems) {
            if (item !is HomeListItems.HomeCategoryItem) return

            val resolvedColor = ContextCompat.getColor(itemView.context, item.category.color)

            iconView.setImageResource(item.category.icon)
            iconView.setColorFilter(resolvedColor)
            nameText.text = item.category.name
            transactionsText.text = "${item.transactionCount} transactions"
            amountText.text = item.amount
            dateText.text = DateUtil.formatDate(item.lastActivityAt)

            itemView.setOnClickListener { onClick?.invoke(item.category) }
        }
    }
}