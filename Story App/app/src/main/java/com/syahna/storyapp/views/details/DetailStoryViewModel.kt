package com.syahna.storyapp.views.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syahna.storyapp.data.repositories.StoriesRepository
import com.syahna.storyapp.remote.response.DetailStoryResponse
import kotlinx.coroutines.launch

class DetailStoryViewModel(private val repository: StoriesRepository) : ViewModel() {

    private val _state = MutableLiveData<DetailState>(DetailState.Idle)
    val state: LiveData<DetailState> = _state

    sealed class DetailState {
        data object Idle : DetailState()
        data object Loading : DetailState()
        data class Success(val detailStory: DetailStoryResponse) : DetailState()
        data class Error(val message: String) : DetailState()
    }

    fun getDetailStory(id: String) {
        _state.value = DetailState.Loading

        viewModelScope.launch {
            try {
                val response = repository.getDetailStory(id)
                if (response.isSuccess) {
                    val body = response.getOrNull()
                    if (body != null) {
                        _state.value = DetailState.Success(body)
                    } else {
                        _state.value = DetailState.Error("Response body is null")
                    }
                } else {
                    _state.value = DetailState.Error("Error: ${response.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _state.value = DetailState.Error("Error: ${e.message}")
            }
        }
    }
}