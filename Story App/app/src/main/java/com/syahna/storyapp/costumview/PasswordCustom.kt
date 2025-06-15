package com.syahna.storyapp.costumview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.syahna.storyapp.R

@SuppressLint("UseCompatLoadingForDrawables")
class PasswordCustom @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnFocusChangeListener {

    private val requiredPasswordLength = 8

    init {
        onFocusChangeListener = this

        addTextChangedListener { s ->
            validatePassword(s.toString())
        }
    }

    private fun validatePassword(password: String) {
        error = if (password.length < requiredPasswordLength) {
            context.getString(R.string.password_error, requiredPasswordLength)
        } else {
            null
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (!hasFocus) {
            validatePassword(text?.toString().orEmpty())
        }
    }
}