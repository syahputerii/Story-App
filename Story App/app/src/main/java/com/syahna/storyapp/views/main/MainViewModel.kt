package com.syahna.storyapp.views.main

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import com.syahna.storyapp.data.pref.UserModel
import com.syahna.storyapp.data.repositories.StoriesRepository
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.remote.response.ListStoryItem
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.paging.cachedIn

class MainViewModel(
    private val userRepository: UserRepository,
    private val storiesRepository: StoriesRepository
) : ViewModel() {

    private val _resultStories = MutableLiveData<List<ListStoryItem>>()
    val resultStories: LiveData<List<ListStoryItem>> = _resultStories

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _resultMaps = MutableLiveData<List<ListStoryItem>>()
    val resultMaps: LiveData<List<ListStoryItem>> = _resultMaps

    val pagingStories: LiveData<PagingData<ListStoryItem>> =
        storiesRepository.getListStoriesPaging().cachedIn(viewModelScope)

    fun getMapsStories() {
        _isLoading.value = true

        viewModelScope.launch {
            val session = userRepository.getSession().firstOrNull()
            if (session == null || session.token.isEmpty()) {
                _errorMessage.value = "User not logged in or token missing."
                _isLoading.value = false
                return@launch
            }

            try {
                val response = storiesRepository.getListStories()
                response.fold(
                    onSuccess = { body ->
                        _resultMaps.value = body.listStory as List<ListStoryItem>
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Error: ${exception.message}"
                        Log.d("MainViewModel", "Error: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
                Log.d("MainViewModel", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}