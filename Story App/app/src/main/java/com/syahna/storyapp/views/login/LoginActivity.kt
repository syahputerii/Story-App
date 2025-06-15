package com.syahna.storyapp.views.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
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
import com.syahna.storyapp.data.pref.UserModel
import com.syahna.storyapp.data.pref.UserPreference
import com.syahna.storyapp.data.pref.dataStore
import com.syahna.storyapp.databinding.ActivityLoginBinding
import com.syahna.storyapp.views.ViewModelFactory
import com.syahna.storyapp.views.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.syahna.storyapp.views.signup.SignupActivity

@Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(this.dataStore)

        setupView()
        setupAction()
        observeLoginResult()
        observeLoadingStatus()
        playAnimation()

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }
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
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_complete_fields), Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launchWhenStarted {
                    viewModel.login(email, password)
                }
            }
        }
    }

    private fun observeLoadingStatus() {
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun observeLoginResult() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginResult.collect { result ->
                result?.let {
                    if (it.isSuccess) {
                        val apiResponse = it.getOrNull()
                        apiResponse?.let { response ->
                            if (!response.error!!) {
                                val loginResult = response.loginResult
                                val email = binding.edLoginEmail.text.toString()

                                val userModel = loginResult?.token?.let { it1 ->
                                    UserModel(
                                        email = email,
                                        token = it1,
                                        isLogin = true
                                    )
                                }

                                if(userModel != null) {
                                    viewModel.saveSession(userModel)
                                    lifecycleScope.launch {
                                        delay(100)
                                        validateUserSession()
                                    }
                                }
                            } else {
                                showErrorDialog(response.message)
                            }
                        }
                    } else {
                        showErrorDialog(result.exceptionOrNull()?.message)
                    }
                }
            }
        }
    }

    private fun validateUserSession() {
        lifecycleScope.launch {
            val userModel = userPreference.getSession().first()
            if (userModel.token.isNotEmpty()) {
                if (userModel.isLogin) {
                    showSuccessDialog(userModel.email)
                } else {
                    showErrorDialog(getString(R.string.error_user_session))
                }
            } else {
                showErrorDialog(getString(R.string.error_token_not_available))
            }
        }
    }

    private fun showErrorDialog(message: String?) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(getString(R.string.error_dialog_title))
            .setContentText(getString(R.string.error_dialog_content, message))
            .setConfirmText(getString(R.string.error_dialog_confirm))
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun showSuccessDialog(email: String) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(getString(R.string.success_dialog_title))
            .setContentText(getString(R.string.success_dialog_content, email))
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, 30f, -30f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val pageTitle = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(300)
        val emailTv = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(300)
        val passwordTv = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(300)
        val emailEt = ObjectAnimator.ofFloat(binding.emailEditText, View.ALPHA, 1f).setDuration(300)
        val passwordEt = ObjectAnimator.ofFloat(binding.passwordEditText, View.ALPHA, 1f).setDuration(300)
        val btnLogin = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(300)


        AnimatorSet().apply {
            playSequentially(
                pageTitle,
                emailEt,
                emailTv,
                passwordEt,
                passwordTv,
                btnLogin
            )
        }.start()
    }
}