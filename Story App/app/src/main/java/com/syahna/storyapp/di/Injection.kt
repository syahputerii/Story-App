package com.syahna.storyapp.di

import android.content.Context
import com.syahna.storyapp.data.pref.UserPreference
import com.syahna.storyapp.data.pref.dataStore
import com.syahna.storyapp.data.repositories.MapsRepository
import com.syahna.storyapp.data.repositories.StoriesRepository
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.remote.retrofit.ApiConfig
import com.syahna.storyapp.remote.retrofit.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {

    fun provideRepository(context: Context, apiService: ApiService = ApiConfig.getApiService("")): UserRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(userPreference, apiService)
    }

    fun provideStoriesRepository(context: Context): StoriesRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { userPreference.getSession().first() }
        val apiService = ApiConfig.getApiService()
        return StoriesRepository.getInstance(apiService, userPreference)
    }
    fun provideLocationRepository(context: Context): MapsRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        return MapsRepository.getInstance(apiService, pref)
    }
}