package com.syahna.storyapp.views.add

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syahna.storyapp.data.repositories.StoriesRepository
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.remote.response.AddStoryResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(private val storiesRepository: StoriesRepository, private val userRepository: UserRepository) : ViewModel()  {
    private val _resultAddStory = MutableLiveData<AddStoryResponse>()
    val resultAddStory: LiveData<AddStoryResponse> = _resultAddStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    suspend fun addNewstory(file: File, description: String, lat: Double?, lon: Double?) {
        _isLoading.value = true

        viewModelScope.launch {
            val session = userRepository.getSession().firstOrNull()
            if (session == null || session.token.isEmpty()) {
                _errorMessage.value = "User not logged in or token missing."
                _isLoading.value = false
                return@launch
            }
            try {
                val response = storiesRepository.addNewStory(description, file, lat, lon)

                response.fold(
                    onSuccess = { body ->
                        _resultAddStory.value = body
                        Log.d("MainViewModel", "List stories: ${_resultAddStory.value}")
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Error: ${exception.message}"
                        Log.d("MainViewModel", "Error: ${exception.message}")
                    }
                )

            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}