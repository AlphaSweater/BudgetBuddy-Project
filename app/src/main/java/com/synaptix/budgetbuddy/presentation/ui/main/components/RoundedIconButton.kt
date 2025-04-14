package com.synaptix.budgetbuddy.presentation.ui.main.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.synaptix.budgetbuddy.R

class RoundedIconButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var iconView: ImageView
    private lateinit var textView: TextView
    private lateinit var container: LinearLayout

    init {
        orientation = HORIZONTAL

        val layoutId: Int

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RoundedIconButton,
            0, 0
        ).apply {
            try {
                layoutId = getResourceId(
                    R.styleable.RoundedIconButton_layoutResource,
                    R.layout.component_rounded_icon_style_button // fallback default
                )
            } finally {
                recycle()
            }
        }

        LayoutInflater.from(context).inflate(layoutId, this, true)

        iconView = findViewById(R.id.buttonIcon)
        textView = findViewById(R.id.buttonText)
        container = findViewById(R.id.buttonContainer)

        context.theme.obtainStyledAttributes(attrs, R.styleable.RoundedIconButton, 0, 0).apply {
            try {
                getString(R.styleable.RoundedIconButton_buttonText)?.let {
                    textView.text = it
                }

                getDrawable(R.styleable.RoundedIconButton_iconDrawable)?.let {
                    iconView.setImageDrawable(it)
                    iconView.visibility = VISIBLE
                }

                getDrawable(R.styleable.RoundedIconButton_buttonBackground)?.let {
                    container.background = it
                }

                val textColor = getColor(R.styleable.RoundedIconButton_textColor, -1)
                if (textColor != -1) {
                    textView.setTextColor(textColor)
                }

            } finally {
                recycle()
            }
        }
    }
}
