package com.syahna.storyapp.views.details

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.syahna.storyapp.databinding.ActivityDetailStoryBinding
import com.syahna.storyapp.remote.response.DetailStoryResponse
import com.syahna.storyapp.views.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding
    private val viewModel by viewModels<DetailStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idStory = intent.getStringExtra(EXTRA_ID_STORY)
        idStory?.let {
            fetchStoryDetail(it)
        } ?: run {
            showErrorToast("Story ID is missing.")
        }

        observeViewModel()
    }

    private fun fetchStoryDetail(id: String) {
        lifecycleScope.launch {
            viewModel.getDetailStory(id)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is DetailStoryViewModel.DetailState.Idle -> {
                }
                is DetailStoryViewModel.DetailState.Loading -> {
                    handleLoadingState(true)
                }
                is DetailStoryViewModel.DetailState.Success -> {
                    handleLoadingState(false)
                    updateUIWithStory(state.detailStory)
                }
                is DetailStoryViewModel.DetailState.Error -> {
                    handleLoadingState(false)
                    showErrorToast(state.message)
                }
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showErrorToast(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun updateUIWithStory(detailStory: DetailStoryResponse) {
        binding.apply {
            tvDetailName.text = detailStory.story?.name ?: "Unknown"
            tvDetailDescription.text = detailStory.story?.description ?: "No description available"
            storyCreatedAt.text = formatDate(detailStory.story?.createdAt)

            loadImageWithGlide(detailStory.story?.photoUrl)
        }
    }

    private fun loadImageWithGlide(photoUrl: String?) {
        Glide.with(this)
            .load(photoUrl)
            .apply(RequestOptions().transform(RoundedCorners(14)))
            .into(binding.ivDetailPhoto)
    }

    private fun formatDate(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return "No date available"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(isoString)
            outputFormat.format(date ?: Date())
        } catch (e: ParseException) {
            Log.e("DetailStoryActivity", "Date parsing error: ${e.message}")
            "Invalid date format"
        }
    }
    companion object {
        const val EXTRA_ID_STORY = "EXTRA_ID_STORY"
    }
}