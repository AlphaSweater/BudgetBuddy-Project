package com.synaptix.budgetbuddy.presentation.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class AnimatedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var isAnimating = false

    private var loadingText: String = ""
    private var successText: String = ""
    private var loadingSpinner: Drawable? = null
    private var successBackground: Drawable? = null
    private var originalBackgroundDrawable: Drawable? = null
    private var originalTextColorInt: Int = currentTextColor
    private var successTextColorInt: Int = Color.WHITE

    private val smallWidthDp = 50
    private val animationDuration = 300L

    init {
        // Load custom attrs if set
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AnimatedButton, 0, 0)

            loadingText = a.getString(R.styleable.AnimatedButton_loadingText) ?: ""
            successText = a.getString(R.styleable.AnimatedButton_successText) ?: "Success"

            loadingSpinner = a.getDrawable(R.styleable.AnimatedButton_loadingSpinner)
            successBackground = a.getDrawable(R.styleable.AnimatedButton_successBackground)
            originalBackgroundDrawable = a.getDrawable(R.styleable.AnimatedButton_originalBackground)

            originalTextColorInt = a.getColor(R.styleable.AnimatedButton_originalTextColor, currentTextColor)
            successTextColorInt = a.getColor(R.styleable.AnimatedButton_successTextColor, Color.WHITE)

            a.recycle()
        }

        if (originalBackgroundDrawable != null) {
            background = originalBackgroundDrawable
        }
    }

    fun startLoading() {
        if (isAnimating) return
        isAnimating = true
        isEnabled = false

        // Save original width to restore later
        val originalWidth = width.takeIf { it > 0 } ?: 100.dpToPx()
        val smallWidth = smallWidthDp.dpToPx()

        // Animate width shrink
        animateWidth(originalWidth, smallWidth)
        text = loadingText
        setCompoundDrawablesWithIntrinsicBounds(null, null, loadingSpinner, null)
        startSpinnerAnimation()

        // Schedule success and reset
        postDelayed({ showSuccess() }, 1600)
        postDelayed({ reset(originalWidth) }, 2900)
    }

    private fun animateWidth(from: Int, to: Int) {
        val animator = ValueAnimator.ofInt(from, to)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            layoutParams.width = value
            requestLayout()
        }
        animator.duration = animationDuration
        animator.start()
    }

    private fun startSpinnerAnimation() {
        val drawable = compoundDrawables[2] ?: return
        ObjectAnimator.ofFloat(drawable, "rotation", 0f, 360f).apply {
            duration = 300
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }

    private fun showSuccess() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        background = successBackground ?: background
        setTextColor(successTextColorInt)
        text = successText

        animate().scaleX(1.1f).scaleY(1.1f).setDuration(300).withEndAction {
            animate().scaleX(1f).scaleY(1f).duration = 300
        }.start()
    }

    private fun reset(originalWidth: Int) {
        animateWidth(width, originalWidth)
        background = originalBackgroundDrawable ?: background
        setTextColor(originalTextColorInt)
        text = "Go" // or keep as original text if you want, can add another attribute
        isEnabled = true
        isAnimating = false
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
