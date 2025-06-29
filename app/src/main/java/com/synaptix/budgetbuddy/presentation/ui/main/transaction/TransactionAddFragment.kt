package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.core.model.Category
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.databinding.FragmentTransactionAddBinding
import com.synaptix.budgetbuddy.extentions.getThemeColor
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel.ScreenMode
import com.synaptix.budgetbuddy.core.util.CurrencyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TransactionAddFragment : Fragment() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Properties
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    private val transactionAddViewModel: TransactionAddViewModel by navGraphViewModels(R.id.ind_transaction_navigation_graph) {defaultViewModelProviderFactory}

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var tempImageUri: Uri? = null

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Fragment Lifecycle
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupImagePickers()
        observeViewModel()

        applyScreenMode(transactionAddViewModel.screenMode.value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // View Setup
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setupViews() {
        setupCurrencySpinner()
        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupCurrencySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            listOf("ZAR")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupTextWatchers() {
        setupAmountWatcher()
        setupNoteWatcher()
    }

    private fun setupAmountWatcher() {
        var current = transactionAddViewModel.amount.value.toString()

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != current) {
                    binding.edtTextAmount.removeTextChangedListener(this)

                    // Remove any non-digit characters
                    val cleanString = newText.replace("[^\\d]".toRegex(), "")
                    
                    // Check if the number is too long (12 digits before decimal + 2 after)
                    if (cleanString.length > 14) {
                        // Keep only the first 14 digits
                        val truncatedString = cleanString.substring(0, 14)
                        val parsed = BigDecimal(truncatedString)
                            .setScale(2, RoundingMode.FLOOR)
                            .divide(BigDecimal(100))
                        
                        val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                        current = formatted
                        binding.edtTextAmount.setText(formatted)
                        binding.edtTextAmount.setSelection(formatted.length)
                        transactionAddViewModel.setAmount(parsed.toDouble())
                        updateAmountAppearance(parsed.toDouble())
                    } else {
                        val parsed = if (cleanString.isNotEmpty()) {
                            BigDecimal(cleanString)
                                .setScale(2, RoundingMode.FLOOR)
                                .divide(BigDecimal(100))
                        } else {
                            BigDecimal.ZERO
                        }

                        if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                            current = ""
                            binding.edtTextAmount.setText("")
                            transactionAddViewModel.setAmount(0.0)
                            updateAmountAppearance(0.0)
                        } else {
                            val formatted = CurrencyUtil.formatWithoutSymbol(parsed.toDouble())
                            current = formatted
                            binding.edtTextAmount.setText(formatted)
                            binding.edtTextAmount.setSelection(formatted.length)
                            transactionAddViewModel.setAmount(parsed.toDouble())
                            updateAmountAppearance(parsed.toDouble())
                        }
                    }

                    binding.edtTextAmount.addTextChangedListener(this)
                }
            }
        }
        binding.edtTextAmount.addTextChangedListener(watcher)
        binding.edtTextAmount.tag = watcher
    }

    private fun setupNoteWatcher() {
        val watcher = binding.edtTextNote.doAfterTextChanged { text ->
            transactionAddViewModel.setNote(text.toString())
        }
        binding.edtTextNote.tag = watcher
    }

    private fun setupImagePickers() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempImageUri != null) {
                handleImageResult(tempImageUri!!)
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleImageResult(it) }
        }

        binding.imagePreview.setOnClickListener {
            showFullScreenImage()
        }

        binding.btnRemovePhoto.setOnClickListener {
            removePhoto()
        }
    }

    private fun populateInitialFormValues() {
        val amount = transactionAddViewModel.amount.value ?: 0.0
        val note = transactionAddViewModel.note.value.orEmpty()

        // Format the amount without triggering the TextWatcher
        val formattedAmount = if (amount == 0.0) "" else DecimalFormat("#,##0.00").format(amount)

        // Set values manually
        binding.edtTextAmount.setText(formattedAmount)
        binding.edtTextNote.setText(note)

        // Re-attach TextWatchers
        setupAmountWatcher()
        setupNoteWatcher()

        // Update appearance
        updateAmountAppearance(amount)
        updateCategoryAppearance(transactionAddViewModel.category.value)
        updateWalletAppearance(transactionAddViewModel.wallet.value)
        updateDateAppearance(transactionAddViewModel.date.value)
        updateRecurrenceAppearance(transactionAddViewModel.recurrenceData.value)
        updateLabelsAppearance(transactionAddViewModel.selectedLabels.value)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Screen Mode Handling
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun applyScreenMode(mode: ScreenMode) {
        when (mode) {
            ScreenMode.VIEW -> applyViewMode()
            ScreenMode.EDIT -> applyEditMode()
            ScreenMode.CREATE -> applyCreateMode()
        }
    }

    private fun applyViewMode() {
        binding.apply {
            btnEdit.visibility = View.VISIBLE
            btnClear.visibility = View.GONE
            btnSave.visibility = View.GONE
            bottomButtonContainer.visibility = View.GONE
            toolbarTitle.text = "Transaction Details"
            
            // Update content margin when bottom container is hidden
            contentScrollView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_margin_no_button)
            }

            disableAllInteractiveElements()
        }
    }

    private fun applyEditMode() {
        binding.apply {
            btnEdit.visibility = View.GONE
            btnClear.visibility = View.VISIBLE
            btnSave.apply {
                text = "Update"
                visibility = View.VISIBLE
            }
            bottomButtonContainer.visibility = View.VISIBLE
            toolbarTitle.text = "Edit Transaction"
            
            // Update content margin when bottom container is visible
            contentScrollView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_margin)
            }

            enableAllInteractiveElements()
            btnRemovePhoto.visibility = if (transactionAddViewModel.imageBytes.value != null) View.VISIBLE else View.GONE
        }
    }

    private fun applyCreateMode() {
        binding.apply {
            btnEdit.visibility = View.GONE
            btnClear.visibility = View.VISIBLE
            btnSave.apply {
                text = "Save"
                visibility = View.VISIBLE
            }
            bottomButtonContainer.visibility = View.VISIBLE
            toolbarTitle.text = "Add New Transaction"
            
            // Update content margin when bottom container is visible
            contentScrollView.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_margin)
            }

            enableAllInteractiveElements()
            btnRemovePhoto.visibility = if (transactionAddViewModel.imageBytes.value != null) View.VISIBLE else View.GONE
        }
    }

    private fun disableAllInteractiveElements() {
        binding.apply {
            edtTextAmount.isEnabled = false
            spinnerCurrency.isEnabled = false
            edtTextNote.isEnabled = false
            rowSelectCategory.isEnabled = false
            rowSelectWallet.isEnabled = false
            rowSelectLabels.isEnabled = false
            rowSelectDate.isEnabled = false
            rowSelectRecurrenceRate.isEnabled = false
            rowSelectPhoto.isEnabled = false
            btnRemovePhoto.visibility = View.GONE

            // Hide all croc icons
            imgCrocCategory.visibility = View.INVISIBLE
            imgCrocWallet.visibility = View.INVISIBLE
            imgCrocDate.visibility = View.INVISIBLE
            imgCrocLabels.visibility = View.INVISIBLE
            imgCrocRecurrence.visibility = View.INVISIBLE
            imgCrocPhoto.visibility = View.INVISIBLE

            rowSelectCategory.setOnClickListener(null)
            rowSelectWallet.setOnClickListener(null)
            rowSelectLabels.setOnClickListener(null)
            rowSelectDate.setOnClickListener(null)
            rowSelectRecurrenceRate.setOnClickListener(null)
            rowSelectPhoto.setOnClickListener(null)
        }
    }

    private fun enableAllInteractiveElements() {
        binding.apply {
            edtTextAmount.isEnabled = true
            spinnerCurrency.isEnabled = true
            edtTextNote.isEnabled = true
            rowSelectCategory.isEnabled = true
            rowSelectWallet.isEnabled = true
            rowSelectLabels.isEnabled = true
            rowSelectDate.isEnabled = true
            rowSelectRecurrenceRate.isEnabled = true
            rowSelectPhoto.isEnabled = true

            // Reattach click listeners
            rowSelectCategory.setOnClickListener { showCategorySelector() }
            rowSelectWallet.setOnClickListener { showWalletSelector() }
            rowSelectLabels.setOnClickListener { showLabelsSelector() }
            rowSelectDate.setOnClickListener { showDatePicker() }
            rowSelectRecurrenceRate.setOnClickListener { showRecurrenceSelector() }
            rowSelectPhoto.setOnClickListener { showImageSourceDialog() }

            // Show all croc icons
            imgCrocCategory.visibility = View.VISIBLE
            imgCrocWallet.visibility = View.VISIBLE
            imgCrocDate.visibility = View.VISIBLE
            imgCrocLabels.visibility = View.VISIBLE
            imgCrocRecurrence.visibility = View.VISIBLE
            imgCrocPhoto.visibility = View.VISIBLE
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Click Listeners
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setupClickListeners() {
        with(binding) {
            btnGoBack.setOnClickListener {
                when (transactionAddViewModel.screenMode.value) {
                    TransactionAddViewModel.ScreenMode.EDIT -> {
                        if (transactionAddViewModel.hasUnsavedChanges.value) {
                            showDiscardChangesDialog()
                        } else {
                            switchToViewMode()
                        }
                    }
                    TransactionAddViewModel.ScreenMode.VIEW,
                    TransactionAddViewModel.ScreenMode.CREATE -> {
                        findNavController().popBackStack()
                    }
                }
            }

            btnClear.setOnClickListener {
                reset()
            }

            btnEdit.setOnClickListener {
                transactionAddViewModel.setScreenMode(ScreenMode.EDIT)
                applyScreenMode(ScreenMode.EDIT)
            }

            rowSelectCategory.setOnClickListener {
                showCategorySelector()
            }

            rowSelectWallet.setOnClickListener {
                showWalletSelector()
            }

            rowSelectLabels.setOnClickListener {
                showLabelsSelector()
            }

            rowSelectDate.setOnClickListener {
                showDatePicker()
            }

            rowSelectRecurrenceRate.setOnClickListener {
                showRecurrenceSelector()
            }

            rowSelectPhoto.setOnClickListener {
                showImageSourceDialog()
            }

            btnSave.setOnClickListener {
                saveTransaction()
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Navigation Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun showLabelsSelector() {
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectLabelFragment)
    }

    private fun showWalletSelector() {
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectWalletFragment)
    }

    private fun showCategorySelector() {
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectCategoryFragment)
    }

    private fun showRecurrenceSelector() {
        findNavController().navigate(R.id.action_transactionAddFragment_to_transactionSelectRecurrenceFragment)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Image Handling
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun handleImageResult(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                transactionAddViewModel.setImageBytes(bytes)
                showImagePreview(bytes)
            }
        } catch (e: Exception) {
            showError("Failed to process image")
        }
    }

    private fun showImagePreview(bytes: ByteArray?) {
        if (bytes == null) {
            binding.imagePreviewContainer.visibility = View.GONE
            return
        }

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imagePreviewContainer.visibility = View.VISIBLE
        binding.imagePreview.setImageBitmap(bitmap)
    }

    private fun removePhoto() {
        transactionAddViewModel.setImageBytes(null)
        binding.imagePreviewContainer.visibility = View.GONE
        binding.imagePreview.setImageBitmap(null)
    }

    private fun showFullScreenImage() {
        transactionAddViewModel.imageBytes.value?.let { bytes ->
            val dialog = AlertDialog.Builder(requireContext(), R.style.FullScreenDialog)
                .setView(
                    ImageView(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                    }
                )
                .create()

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> pickImageLauncher.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "transaction_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        
        try {
            tempImageUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            tempImageUri?.let { uri ->
                takePictureLauncher.launch(uri)
            } ?: showError("Failed to prepare camera")
        } catch (e: Exception) {
            showError("Failed to launch camera")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI Update Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\

    private fun updateSelectedCategory(category: Category?) {
        if (category == null) {
            binding.textSelectedCategoryName.text = "Select category"
            binding.imgSelectedCategoryIcon.setImageResource(R.drawable.ic_ui_categories)
        } else {
            binding.textSelectedCategoryName.text = category.name
            binding.imgSelectedCategoryIcon.setImageResource(category.icon)
        }
        updateCategoryAppearance(category)
        updateAmountAppearance(transactionAddViewModel.amount.value)
    }

    private fun updateSelectedWallet(wallet: Wallet?) {
        binding.textSelectedWalletName.text = wallet?.name ?: "Select wallet"
        updateWalletAppearance(wallet)
    }

    private fun updateSelectedDate(date: String?) {
        if (date == null) {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            binding.textSelectedDate.text = currentDate.format(formatter)
        } else {
            binding.textSelectedDate.text = date
        }
        updateDateAppearance(date)
    }

    private fun updateSelectedLabels(labels: List<Label>) {
        binding.chipGroupLabels.removeAllViews()

        if (labels.isEmpty()) {
            binding.textSelectedLabels.visibility = View.VISIBLE
        } else {
            binding.textSelectedLabels.visibility = View.GONE

            labels.forEach { label ->
                val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = label.name
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        transactionAddViewModel.removeLabel(label)
                    }
                }
                binding.chipGroupLabels.addView(chip)
            }
        }
        updateLabelsAppearance(labels)
    }

    private fun updateSelectedRecurrence(recurrence: RecurrenceData) {
        binding.textSelectedRecurrenceRate.text = recurrence.toDisplayString()
        updateRecurrenceAppearance(recurrence)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Form Handling
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun saveTransaction() {
        val date = binding.textSelectedDate.text.toString()
        transactionAddViewModel.setDate(date)

        val note = binding.edtTextNote.text.toString()
        transactionAddViewModel.setNote(note)

        val currency = binding.spinnerCurrency.selectedItem.toString()
        transactionAddViewModel.setCurrency(currency)

        transactionAddViewModel.showValidationErrors()
        if (!transactionAddViewModel.validateForm()) return

        transactionAddViewModel.addTransaction()
    }

    private fun reset() {
        transactionAddViewModel.reset()
        binding.apply {
            edtTextAmount.setText("")
            edtTextNote.setText("")
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI State Handlers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun handleLoadingUiState(state: TransactionAddViewModel.LoadingUiState) {
        when (state) {
            is TransactionAddViewModel.LoadingUiState.Loading -> {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successCheckmark.visibility = View.GONE
                binding.loadingText.text = "Loading..."
            }
            is TransactionAddViewModel.LoadingUiState.Loaded -> {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                populateInitialFormValues()
            }
            is TransactionAddViewModel.LoadingUiState.Error -> {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                showError(state.message)
            }
            else -> {
                binding.loadingOverlay.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleSavingUiState(state: TransactionAddViewModel.SavingUiState) {
        when (state) {
            is TransactionAddViewModel.SavingUiState.Saving -> {
                binding.btnSave.isEnabled = false
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successCheckmark.visibility = View.GONE
                binding.loadingText.text = when (transactionAddViewModel.screenMode.value) {
                    TransactionAddViewModel.ScreenMode.EDIT -> "Updating transaction..."
                    else -> "Saving transaction..."
                }
            }
            is TransactionAddViewModel.SavingUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.successCheckmark.visibility = View.VISIBLE
                binding.loadingText.text = when (transactionAddViewModel.screenMode.value) {
                    TransactionAddViewModel.ScreenMode.EDIT -> "Transaction updated successfully!"
                    else -> "Transaction saved successfully!"
                }
                
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    binding.loadingOverlay.visibility = View.GONE
                    transactionAddViewModel.reset()
                    findNavController().popBackStack()
                }
            }
            is TransactionAddViewModel.SavingUiState.Error -> {
                binding.btnSave.isEnabled = true
                binding.loadingOverlay.visibility = View.GONE
                showError(state.message)
            }
            else -> {
                binding.btnSave.isEnabled = true
                binding.loadingOverlay.visibility = View.GONE
            }
        }
    }

    private fun handleValidationState(state: TransactionAddViewModel.ValidationState) {
        with(binding) {
            textAmountError.apply {
                text = state.amountError
                visibility = if (state.shouldShowErrors && state.amountError != null) View.VISIBLE else View.GONE
            }

            textCurrencyError.apply {
                text = state.currencyError
                visibility = if (state.shouldShowErrors && state.currencyError != null) View.VISIBLE else View.GONE
            }

            textCategoryError.apply {
                text = state.categoryError
                visibility = if (state.shouldShowErrors && state.categoryError != null) View.VISIBLE else View.GONE
            }

            textWalletError.apply {
                text = state.walletError
                visibility = if (state.shouldShowErrors && state.walletError != null) View.VISIBLE else View.GONE
            }

            textDateError.apply {
                text = state.dateError
                visibility = if (state.shouldShowErrors && state.dateError != null) View.VISIBLE else View.GONE
            }
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // UI Formatting
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun updateAmountAppearance(amount: Double?) {
        val category = transactionAddViewModel.category.value
        binding.apply {
            // Update amount field color
            edtTextAmount.setTextColor(
                when {
                    category == null -> requireContext().getThemeColor(R.attr.bb_primaryText)
                    category.type == "expense" -> requireContext().getColor(R.color.expense_red)
                    category.type == "income" -> requireContext().getColor(R.color.profit_green)
                    else -> requireContext().getThemeColor(R.attr.bb_primaryText)
                }
            )

            // Update sign visibility and text
            textAmountSign.apply {
                visibility = if (category != null && amount != null && amount != 0.0) View.VISIBLE else View.GONE
                text = when (category?.type) {
                    "expense" -> "- "
                    "income" -> "+ "
                    else -> ""
                }
                setTextColor(
                    when (category?.type) {
                        "expense" -> requireContext().getColor(R.color.expense_red)
                        "income" -> requireContext().getColor(R.color.profit_green)
                        else -> requireContext().getThemeColor(R.attr.bb_primaryText)
                    }
                )
            }
        }
    }

    private fun formatDate(date: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(Date(date))
    }

    private fun updateCategoryAppearance(category: Category?) {
        binding.apply {
            if (category == null) {
                imgSelectedCategoryIcon.setColorFilter(requireContext().getThemeColor(R.attr.bb_accent))
                textSelectedCategoryName.setTextColor(requireContext().getThemeColor(R.attr.bb_secondaryText))
            } else {
                imgSelectedCategoryIcon.setColorFilter(requireContext().getColor(category.color))
                textSelectedCategoryName.setTextColor(requireContext().getThemeColor(R.attr.bb_primaryText))
            }
        }
    }

    private fun updateWalletAppearance(wallet: Wallet?) {
        binding.apply {
            textSelectedWalletName.setTextColor(
                if (wallet == null) {
                    requireContext().getThemeColor(R.attr.bb_secondaryText)
                } else {
                    requireContext().getThemeColor(R.attr.bb_primaryText)
                }
            )
        }
    }

    private fun updateDateAppearance(date: String?) {
        binding.apply {
            textSelectedDate.setTextColor(
                if (date == null) {
                    requireContext().getThemeColor(R.attr.bb_secondaryText)
                } else {
                    requireContext().getThemeColor(R.attr.bb_primaryText)
                }
            )
        }
    }

    private fun updateRecurrenceAppearance(recurrence: RecurrenceData) {
        binding.apply {
            textSelectedRecurrenceRate.setTextColor(
                if (recurrence == RecurrenceData.DEFAULT) {
                    requireContext().getThemeColor(R.attr.bb_secondaryText)
                } else {
                    requireContext().getThemeColor(R.attr.bb_primaryText)
                }
            )
        }
    }

    private fun updateLabelsAppearance(labels: List<Label>) {
        binding.apply {
            textSelectedLabels.setTextColor(
                if (labels.isEmpty()) {
                    requireContext().getThemeColor(R.attr.bb_secondaryText)
                } else {
                    requireContext().getThemeColor(R.attr.bb_primaryText)
                }
            )
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Helper Methods
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(selection))
            transactionAddViewModel.setDate(date)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }

    private fun showDiscardChangesDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Rounded)
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Discard") { _, _ ->
                // Temporarily remove text watchers
                binding.edtTextAmount.removeTextChangedListener(binding.edtTextAmount.tag as? TextWatcher)
                binding.edtTextNote.removeTextChangedListener(binding.edtTextNote.tag as? TextWatcher)

                // Revert changes in ViewModel
                transactionAddViewModel.revertChanges()

                // Update text fields with reverted values
                val amount = transactionAddViewModel.amount.value
                val formattedAmount = if (amount == 0.0) "" else DecimalFormat("#,##0.00").format(amount)
                binding.edtTextAmount.setText(formattedAmount)
                binding.edtTextNote.setText(transactionAddViewModel.note.value)

                // Reattach text watchers
                setupAmountWatcher()
                setupNoteWatcher()

                // Switch to view mode
                switchToViewMode()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            // Set button colors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.expense_red)
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.profit_green)
            )

            val background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog_rounded)
            dialog.window?.setBackgroundDrawable(background)
        }

        dialog.show()
    }

    private fun switchToViewMode() {
        transactionAddViewModel.setScreenMode(TransactionAddViewModel.ScreenMode.VIEW)
        applyScreenMode(TransactionAddViewModel.ScreenMode.VIEW)
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // ViewModel Observers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { transactionAddViewModel.screenMode.collect { mode ->
                    applyScreenMode(mode)
                } }
                launch { transactionAddViewModel.loadingUiState.collect { state ->
                    handleLoadingUiState(state)
                } }
                launch { transactionAddViewModel.savingUiState.collect { state ->
                    handleSavingUiState(state)
                } }
                launch { transactionAddViewModel.validationState.collect { state ->
                    handleValidationState(state)
                } }
                launch { transactionAddViewModel.category.collect { category ->
                    updateSelectedCategory(category)
                } }
                launch { transactionAddViewModel.wallet.collect { wallet ->
                    updateSelectedWallet(wallet)
                } }
                launch { transactionAddViewModel.date.collect { date ->
                    updateSelectedDate(date)
                } }
                launch { transactionAddViewModel.recurrenceData.collect { recurrence ->
                    updateSelectedRecurrence(recurrence)
                } }
                launch { transactionAddViewModel.selectedLabels.collect { labels ->
                    updateSelectedLabels(labels)
                } }
                launch { transactionAddViewModel.transaction.collect { transaction ->
                    transaction?.let {
                        // Load image if available
                        if (it.photoUrl != null) {
                            transactionAddViewModel.loadImageFromUrl(it.photoUrl)
                        }
                    }
                } }
                launch { transactionAddViewModel.imageBytes.collect { bytes ->
                    if (bytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.imagePreview.apply {
                            setImageBitmap(bitmap)
                            visibility = View.VISIBLE
                        }
                        binding.imagePreviewContainer.visibility = View.VISIBLE
                    } else {
                        binding.imagePreview.visibility = View.GONE
                        binding.imagePreviewContainer.visibility = View.GONE
                    }
                } }
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\