package com.syahna.storyapp.views.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syahna.storyapp.R
import com.syahna.storyapp.databinding.ActivitySignupBinding
import com.syahna.storyapp.views.ViewModelFactory

@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    private val signupViewModel: SignupViewModel by viewModels {
        ViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        observeRegisterResult()
        observeLoading()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val email = binding.edRegisterEmail.text.toString().trim()
            val name = binding.edRegisterName.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_email_empty), Toast.LENGTH_SHORT).show()
                }

                name.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_name_empty), Toast.LENGTH_SHORT).show()
                }

                password.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.error_password_empty), Toast.LENGTH_SHORT).show()
                }

                password.length < 8 -> {
                    binding.edRegisterPassword.error = getString(R.string.error_password_length)
                    Toast.makeText(this, getString(R.string.error_password_length), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    lifecycleScope.launchWhenStarted {
                        signupViewModel.performRegister(name, email, password)
                    }
                }
            }
        }
    }

    private fun observeRegisterResult() {
        lifecycleScope.launchWhenStarted{
            signupViewModel.signupResult.collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        val email = binding.edRegisterEmail.text.toString()
                        showSuccessDialog(email)
                    } else {
                        showErrorDialog(it.exceptionOrNull()?.message)
                    }
                }
            }
        }
    }

    private fun observeLoading() {
        lifecycleScope.launchWhenStarted {
            signupViewModel.loadingStatus.collect { isLoading ->
                binding.progressBarSignup.visibility = if (isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun showErrorDialog(message: String?) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(getString(R.string.dialog_error_title))
            .setContentText(getString(R.string.dialog_error_content, message))
            .setConfirmText(getString(R.string.dialog_confirm_ok))
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun showSuccessDialog(email: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(getString(R.string.dialog_success_title))
            .setContentText(getString(R.string.dialog_success_content, email))
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                finish()
            }
            .show()
    }
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val pageTitle = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(300)
        val nameTv = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(300)
        val emailTv = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(300)
        val passwordTv = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(300)
        val nameEt = ObjectAnimator.ofFloat(binding.nameEditText, View.ALPHA, 1f).setDuration(300)
        val emailEt = ObjectAnimator.ofFloat(binding.emailEditText, View.ALPHA, 1f).setDuration(300)
        val passwordEt = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(300)
        val registerBtn = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(
                pageTitle,
                nameEt,
                nameTv,
                emailEt,
                emailTv,
                passwordEt,
                passwordTv,
                registerBtn
            )
            startDelay = 300L
        }.start()
    }
}
