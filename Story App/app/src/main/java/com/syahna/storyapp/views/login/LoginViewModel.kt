package com.syahna.storyapp.views.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syahna.storyapp.data.pref.UserModel
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.remote.response.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResult = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginResult: StateFlow<Result<LoginResponse>?> = _loginResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    suspend fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.login(email, password)
                _loginResult.value = result
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
                Log.d("LoginViewModel", "Login failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}