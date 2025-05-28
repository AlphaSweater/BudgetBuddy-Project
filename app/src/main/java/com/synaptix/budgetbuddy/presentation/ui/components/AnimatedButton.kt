package com.synaptix.budgetbuddy.presentation.ui.components

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat.getColorStateList
import com.synaptix.budgetbuddy.R
import androidx.core.content.withStyledAttributes

class AnimatedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val button = AppCompatButton(context).apply {
        isAllCaps = false
        textSize = 18f
        gravity = Gravity.CENTER
        elevation = 4f
    }
    
    private val progressBar = ProgressBar(context).apply {
        visibility = GONE
        isIndeterminate = true
        indeterminateTintList = getColorStateList(context, android.R.color.white)
        scaleX = 0.8f
        scaleY = 0.8f
    }

    private var isAnimating = false
    private val handler = Handler(Looper.getMainLooper())
    private var originalWidth = 0

    private var loadingText = "Loading..."
    private var successText = "Success"
    private var errorText = "Error"
    private var originalText = "Go"

    private var originalBackgroundDrawable: Drawable? = null
    private var successBackground: Drawable? = null
    private var errorBackground: Drawable? = null
    private var originalTextColor = Color.WHITE
    private var successTextColor = Color.WHITE
    private var errorTextColor = Color.WHITE

    private var originalTextStyle: Int = 0

    private val animationDuration = 300L
    private val successScale = 1.05f
    private val errorScale = 0.95f

    // Add callback for click events
    private var onButtonClickListener: (() -> Unit)? = null

    init {
        // Inflate attributes
        context.withStyledAttributes(attrs, R.styleable.AnimatedButton) {
            loadingText = getString(R.styleable.AnimatedButton_loadingText) ?: loadingText
            successText = getString(R.styleable.AnimatedButton_successText) ?: successText
            errorText = getString(R.styleable.AnimatedButton_errorText) ?: errorText
            originalText = getString(R.styleable.AnimatedButton_originalText) ?: originalText

            originalBackgroundDrawable = getDrawable(R.styleable.AnimatedButton_originalBackground)
            successBackground = getDrawable(R.styleable.AnimatedButton_successBackground)
            errorBackground = getDrawable(R.styleable.AnimatedButton_errorBackground)

            originalTextColor = getColor(R.styleable.AnimatedButton_originalTextColor, Color.WHITE)
            successTextColor = getColor(R.styleable.AnimatedButton_successTextColor, Color.WHITE)
            errorTextColor = getColor(R.styleable.AnimatedButton_errorTextColor, Color.WHITE)

            originalTextStyle = getInt(R.styleable.AnimatedButton_originalTextStyle, Typeface.NORMAL)
        }

        // Style and configure button
        button.apply {
            text = originalText
            typeface = Typeface.defaultFromStyle(originalTextStyle)
            background = originalBackgroundDrawable
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(originalTextColor)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Center progress bar
        val progressParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )

        addView(button)
        addView(progressBar, progressParams)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (originalWidth == 0) {
            originalWidth = w
        }
    }

    // Add method to set click listener
    fun setOnButtonClickListener(listener: () -> Unit) {
        onButtonClickListener = listener
    }

    // Override standard click listener
    override fun setOnClickListener(l: OnClickListener?) {
        button.setOnClickListener { 
            if (!isAnimating) {
                l?.onClick(this)
            }
        }
    }

    // Make startLoading public since we need to call it after validation
    fun startLoading() {
        if (isAnimating) return // Prevent multiple starts
        
        isAnimating = true
        button.isEnabled = false

        // Use the current height as the target width to make it perfectly circular
        val targetWidth = height

        // Animate shrink with easing
        animateWidth(originalWidth, targetWidth)

        // Hide text, show spinner with smooth transitions
        button.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(animationDuration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                button.text = ""
                progressBar.visibility = VISIBLE
                progressBar.alpha = 0f
                progressBar.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(animationDuration)
                    .setInterpolator(OvershootInterpolator())
                    .start()

                button.scaleX = 1f
                button.scaleY = 1f
            }.start()
    }

    // Add public methods for success and error states
    fun showSuccess() {
        // Step 1: Restore original width first
        animateWidth(width, originalWidth)

        // Step 2: Hide progress bar with animation
        progressBar.animate()
            .alpha(0f)
            .setDuration(animationDuration)
            .withEndAction {
                progressBar.visibility = GONE

                // Step 3: Now apply success state cleanly
                button.apply {
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 0)
                    scaleX = 1f
                    scaleY = 1f
                    background = successBackground
                    setTextColor(successTextColor)
                    text = successText
                    isEnabled = false
                }

                // Step 4: Animate scale pop for success
                button.animate()
                    .scaleX(successScale)
                    .scaleY(successScale)
                    .setDuration(animationDuration)
                    .setInterpolator(OvershootInterpolator())
                    .withEndAction {
                        button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(animationDuration)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }.start()
            }.start()
    }

    fun showError() {
        // Add a small delay to allow ripple effect to complete
        handler.postDelayed({
            // Step 1: Restore original width first
            animateWidth(width, originalWidth)

            // Step 2: Hide progress bar with animation
            progressBar.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .withEndAction {
                    progressBar.visibility = GONE

                    // Step 3: Apply error state
                    button.apply {
                        gravity = Gravity.CENTER
                        setPadding(0, 0, 0, 0)
                        scaleX = 1f
                        scaleY = 1f
                        background = errorBackground ?: originalBackgroundDrawable
                        setTextColor(errorTextColor)
                        text = errorText
                        isEnabled = true
                    }

                    // Step 4: Animate error shake
                    button.animate()
                        .scaleX(errorScale)
                        .scaleY(errorScale)
                        .setDuration(animationDuration / 2)
                        .withEndAction {
                            button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(animationDuration / 2)
                                .setInterpolator(OvershootInterpolator())
                                .withEndAction {
                                    // Reset animation state after error animation completes
                                    isAnimating = false
                                }
                                .start()
                        }.start()
                }.start()
        }, 130) // 150ms delay to allow ripple to complete
    }

    // Make reset public for error handling
    fun reset() {
        animateWidth(width, originalWidth)
        button.apply {
            background = originalBackgroundDrawable
            setTextColor(originalTextColor)
            text = originalText
            isEnabled = true
            scaleX = 1f
            scaleY = 1f
        }
        progressBar.visibility = GONE
        isAnimating = false
    }

    private fun animateWidth(from: Int, to: Int) {
        ValueAnimator.ofInt(from, to).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                layoutParams.width = it.animatedValue as Int
                requestLayout()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}