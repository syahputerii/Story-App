package com.syahna.storyapp.views.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syahna.storyapp.data.repositories.MapsRepository
import com.syahna.storyapp.remote.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(private val locationRepository: MapsRepository) : ViewModel() {

    private val _mapList = MutableLiveData<List<ListStoryItem>>()
    val mapList: LiveData<List<ListStoryItem>> = _mapList

    fun getLocation() {
        viewModelScope.launch {
            try {
                val stories = locationRepository.getLocation()
                if (stories.isNotEmpty()) {
                    Log.d("MapsViewModel", "Jumlah lokasi yang ditemukan: ${stories.size}")
                    _mapList.postValue(stories)
                } else {
                    Log.w("MapsViewModel", "Tidak ada lokasi yang ditemukan.")
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Kesalahan saat mengambil lokasi: ${e.message}", e)
            }
        }
    }
}