package com.syahna.storyapp.views.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.remote.response.SignupResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _signupResult = MutableStateFlow<Result<SignupResponse>?>(null)
    val signupResult: StateFlow<Result<SignupResponse>?> = _signupResult

    private val _loadingStatus = MutableStateFlow(false)
    val loadingStatus: StateFlow<Boolean> = _loadingStatus

    suspend fun performRegister(userName: String, userEmail: String, userPassword: String) {
        _loadingStatus.value = true

        viewModelScope.launch {
            try {
                _signupResult.value = userRepository.register(userName, userEmail, userPassword)
            } catch (exception: Exception) {
                Log.d("SignupViewModel", "Registration error: ${exception.message}")
            } finally {
                _loadingStatus.value = false
            }
        }
    }
}