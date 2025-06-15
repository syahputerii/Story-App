package com.syahna.storyapp.views

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syahna.storyapp.data.repositories.MapsRepository
import com.syahna.storyapp.data.repositories.StoriesRepository
import com.syahna.storyapp.data.repositories.UserRepository
import com.syahna.storyapp.di.Injection
import com.syahna.storyapp.views.add.AddStoryViewModel
import com.syahna.storyapp.views.details.DetailStoryViewModel
import com.syahna.storyapp.views.login.LoginViewModel
import com.syahna.storyapp.views.main.MainViewModel
import com.syahna.storyapp.views.maps.MapsViewModel
import com.syahna.storyapp.views.signup.SignupViewModel

class ViewModelFactory(private val repository: UserRepository, private val storiesRepository: StoriesRepository, private val locationRepository: MapsRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository, storiesRepository) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(storiesRepository, repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
                SignupViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailStoryViewModel::class.java) -> {
                DetailStoryViewModel(storiesRepository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(locationRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(
                        Injection.provideRepository(context),
                        Injection.provideStoriesRepository(context),
                        Injection.provideLocationRepository(context),
                    )
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}