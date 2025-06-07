package com.synaptix.budgetbuddy.presentation.ui.main.transaction

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.synaptix.budgetbuddy.R
import com.synaptix.budgetbuddy.databinding.FragmentTransactionAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.synaptix.budgetbuddy.core.model.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.ImageView
import com.synaptix.budgetbuddy.core.model.RecurrenceData
import com.synaptix.budgetbuddy.core.model.Wallet
import com.synaptix.budgetbuddy.extentions.getThemeColor
import kotlin.toString
import com.synaptix.budgetbuddy.core.model.Label
import com.synaptix.budgetbuddy.core.model.Transaction
import com.synaptix.budgetbuddy.presentation.ui.main.transaction.TransactionAddViewModel.ScreenMode
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

@AndroidEntryPoint
class TransactionAddFragment : Fragment() {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Properties
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    private val transactionAddViewModel: TransactionAddViewModel by activityViewModels()

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

        // Set up initial state based on screen mode
        if (!transactionAddViewModel.isScreenModeBusy()) {
            val screenMode = arguments?.getSerializable("screenMode") as? ScreenMode ?: ScreenMode.CREATE
            transactionAddViewModel.setScreenMode(screenMode)
            transactionAddViewModel.setScreenModeBusy(true)
        }

        applyScreenMode(transactionAddViewModel.screenMode.value)
        populateInitialFormValues()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!transactionAddViewModel.isScreenModeBusy()) {
            reset()
        }
        _binding = null
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Initial Setup Methods
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

        binding.edtTextAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                if (newText != current) {
                    binding.edtTextAmount.removeTextChangedListener(this)

                    val cleanString = newText.replace("[^\\d]".toRegex(), "")
                    val parsed = if (cleanString.isNotEmpty()) {
                        BigDecimal(cleanString)
                            .setScale(2, RoundingMode.FLOOR)
                            .divide(BigDecimal(100))
                    } else {
                        BigDecimal.ZERO
                    }

                    if (parsed.compareTo(BigDecimal.ZERO) == 0) {
                        // Reset everything if value is exactly zero
                        current = ""
                        binding.edtTextAmount.setText("")
                        transactionAddViewModel.setAmount(0.0)
                        updateAmountAppearance(0.0)
                    } else {
                        val formatted = DecimalFormat("#,##0.00").format(parsed)

                        current = formatted
                        binding.edtTextAmount.setText(formatted)
                        binding.edtTextAmount.setSelection(formatted.length)

                        transactionAddViewModel.setAmount(parsed.toDouble())
                        updateAmountAppearance(parsed.toDouble())
                    }

                    binding.edtTextAmount.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupNoteWatcher() {
        binding.edtTextNote.doAfterTextChanged { text ->
            transactionAddViewModel.setNote(text.toString())
        }
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

            enableAllInteractiveElements()
            btnRemovePhoto.visibility = if (transactionAddViewModel.imageBytes.value != null) View.VISIBLE else View.GONE
        }
        setupClickListeners()
    }

    private fun applyCreateMode() {
        binding.apply {
            btnEdit.visibility = View.GONE
            btnClear.visibility = View.VISIBLE
            btnSave.apply {
                text = "Save"
                visibility = View.VISIBLE
            }

            enableAllInteractiveElements()
            btnRemovePhoto.visibility = if (transactionAddViewModel.imageBytes.value != null) View.VISIBLE else View.GONE
        }
        setupClickListeners()
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
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Click Listeners
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun setupClickListeners() {
        with(binding) {
            btnGoBack.setOnClickListener {
                transactionAddViewModel.setScreenModeBusy(false)
                findNavController().popBackStack()
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
        val amount = binding.edtTextAmount.text.toString().toDoubleOrNull()
        transactionAddViewModel.setAmount(amount)

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
    private fun handleUiState(state: TransactionAddViewModel.UiState) {
        when (state) {
            is TransactionAddViewModel.UiState.Loading -> {
                binding.btnSave.isEnabled = false
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.successCheckmark.visibility = View.GONE
                binding.loadingText.text = "Saving transaction..."
            }
            is TransactionAddViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.successCheckmark.visibility = View.VISIBLE
                binding.loadingText.text = "Transaction saved successfully!"
                
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000)
                    binding.loadingOverlay.visibility = View.GONE
                    transactionAddViewModel.reset()
                    findNavController().popBackStack()
                }
            }
            is TransactionAddViewModel.UiState.Error -> {
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
                    "expense" -> "-"
                    "income" -> "+"
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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // ViewModel Observers
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { transactionAddViewModel.uiState.collect { handleUiState(it) } }
                launch { transactionAddViewModel.validationState.collect { handleValidationState(it) } }
//                launch { transactionAddViewModel.transaction.collect { it?.let { populateFieldsForEdit(it) } } }
                launch { transactionAddViewModel.category.collect { updateSelectedCategory(it) } }
                launch { transactionAddViewModel.wallet.collect { updateSelectedWallet(it) } }
                launch { transactionAddViewModel.date.collect { updateSelectedDate(it) } }
                launch { transactionAddViewModel.recurrenceData.collect { updateSelectedRecurrence(it) } }
                launch { transactionAddViewModel.selectedLabels.collect { updateSelectedLabels(it) } }
                launch { transactionAddViewModel.imageBytes.collect { bytes ->
                    if (bytes != null) {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        binding.imagePreview.apply {
                            setImageBitmap(bitmap)
                            visibility = View.VISIBLE
                        }
                    } else {
                        binding.imagePreview.visibility = View.GONE
                    }
                }}
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\