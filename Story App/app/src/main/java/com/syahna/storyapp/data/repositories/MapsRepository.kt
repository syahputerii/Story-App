package com.syahna.storyapp.data.repositories

import android.util.Log
import com.syahna.storyapp.data.pref.UserPreference
import com.syahna.storyapp.remote.response.ListStoryItem
import com.syahna.storyapp.remote.retrofit.ApiService
import kotlinx.coroutines.flow.firstOrNull

class MapsRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    companion object {
        @Volatile
        private var INSTANCE: MapsRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): MapsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MapsRepository(apiService, userPreference).also { INSTANCE = it }
            }
        }
    }

    suspend fun getLocation(): List<ListStoryItem> {
        return try {
            val token = userPreference.getSession().firstOrNull()?.token
            if (token.isNullOrEmpty()) {
                Log.e("MapsRepository", "Token tidak ditemukan.")
                throw IllegalStateException("Token tidak valid atau kosong.")
            }

            Log.d("MapsRepository", "Menggunakan token: $token")

            val response = apiService.getLocation(location = 1, token = "Bearer $token")

            if (response.listStory.isNullOrEmpty()) {
                Log.w("MapsRepository", "Respons kosong atau tidak valid dari API.")
                return emptyList()
            }

            val validStories = response.listStory.filterNotNull().filter { story ->
                story.lat != null && story.lon != null
            }

            Log.d(
                "MapsRepository",
                "Jumlah cerita valid: ${validStories.size} dari total ${response.listStory.size}"
            )

            validStories
        } catch (e: Exception) {
            Log.e("MapsRepository", "Kesalahan saat mengambil lokasi: ${e.message}", e)
            emptyList()
        }
    }
}