package com.syahna.storyapp.data.repositories

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.syahna.storyapp.data.pref.UserPreference
import com.syahna.storyapp.remote.response.AddStoryResponse
import com.syahna.storyapp.remote.response.DetailStoryResponse
import com.syahna.storyapp.remote.response.ListStoryItem
import com.syahna.storyapp.remote.response.ListStoryResponse
import com.syahna.storyapp.remote.retrofit.ApiService
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import androidx.paging.liveData
import com.syahna.storyapp.data.source.StoryPagingSource

class StoriesRepository(private val apiService: ApiService, private val userPreference: UserPreference) {
    companion object {
        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoriesRepository {
            return StoriesRepository(apiService, userPreference)
        }
    }

    suspend fun getListStories(location: Int? = null,page: Int? = null, size: Int? = null): Result<ListStoryResponse> {
        return try {
            val token = userPreference.getSession().firstOrNull()?.token
            if (token.isNullOrEmpty()) {
                Result.failure<Throwable>(Exception("Authentication token is missing"))
            }

            val response = apiService.getStories("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Server returned an empty response body"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Stories Error with code: ${response.code()}, message: $errorMessage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getListStoriesPaging(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(storiesRepository = this)
            }
        ).liveData
    }


    suspend fun getDetailStory(id: String): Result<DetailStoryResponse> {
        return try {
            val token = userPreference.getSession().firstOrNull()?.token
            if (token.isNullOrEmpty()) {
                Result.failure<Throwable>(Exception("Authentication token is missing"))
            }

            val response = apiService.getDetailStory("Bearer $token", id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Server returned an empty response body"))
                }
            } else {
                Result.failure(Exception("Stories Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addNewStory(
        description: String,
        imageFile: File,
        lat: Double? = null,
        lon: Double? = null
    ): Result<AddStoryResponse> {
        return try {
            val token = userPreference.getSession().firstOrNull()?.token
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token not found"))
            }

            val imagePart = imageFile.toMultipartBody()
            val descriptionPart = description.toRequestBody()
            val latPart = lat?.toString()?.toRequestBody()
            val lonPart = lon?.toString()?.toRequestBody()

            val response = apiService.addNewStory("Bearer $token", imagePart, descriptionPart, latPart, lonPart)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Server returned an empty response body"))
                }
            } else {
                Result.failure(Exception("Stories Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListPaging(
        location: Int? = null,
        page: Int? = null,
        size: Int? = null
    ): List<ListStoryItem> {
        return try {
            val token = userPreference.getSession().firstOrNull()?.token
                ?: throw Exception("Authentication token is missing")

            val response = apiService.getStories("Bearer $token", page, size, location)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    body.listStory?.filterNotNull() ?: emptyList()
                } else {
                    throw Exception("Server returned an empty response body")
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                throw Exception("Stories Error with code: ${response.code()}, message: $errorMessage")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch stories: ${e.message}", e)
        }
    }

    private fun File.toMultipartBody(): MultipartBody.Part {
        val requestBody = this.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", this.name, requestBody)
    }
}