package com.syahna.storyapp.costumview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.syahna.storyapp.R
import java.util.regex.Pattern

@SuppressLint("UseCompatLoadingForDrawables")
class EmailCustom @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    private var clearIconEditText: Drawable =
        ContextCompat.getDrawable(context, R.drawable.ic_baseline_close_24) as Drawable
    private val emailPattern: Pattern =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    init {
        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    displayClearIcon()
                    validateEmailFormat(s.toString())
                } else {
                    removeClearIcon()
                    resetErrorState()
                }
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun displayClearIcon() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, clearIconEditText, null)
    }

    private fun removeClearIcon() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        val drawableEnd = compoundDrawables[2] ?: return false
        val isRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL

        val clearButtonClicked = if (isRtl) {
            (event?.x ?: 0f) < (clearIconEditText.intrinsicWidth + paddingStart).toFloat()
        } else {
            (event?.x ?: 0f) > (width - paddingEnd - clearIconEditText.intrinsicWidth).toFloat()
        }

        if (clearButtonClicked) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    displayClearIcon()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    text?.clear()
                    removeClearIcon()
                    return true
                }
            }
        }
        return false
    }

    private fun validateEmailFormat(email: String) {
        if (!emailPattern.matcher(email).matches()) {
            error = context.getString(R.string.invalid_email_format)
        } else {
            resetErrorState()
        }
    }

    private fun resetErrorState() {
        error = null
    }
}