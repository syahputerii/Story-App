package com.syahna.storyapp.views.main

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syahna.storyapp.databinding.ItemStoryBinding
import com.syahna.storyapp.remote.response.ListStoryItem
import com.syahna.storyapp.views.details.DetailStoryActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainAdapterPaging:
    PagingDataAdapter<ListStoryItem, MainAdapterPaging.StoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder:StoryViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStoryItem) {
            with(binding) {
                tvItemName.text = data.name
                tvItemDescription.text = data.description
                tvItemDate.text = data.createdAt?.let { formatDate(it) }

            Glide.with(itemView.context)
                .load(data.photoUrl)
                .into(ivItemPhoto)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.EXTRA_ID_STORY, data.id)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(ivItemPhoto, "photo"),
                        Pair(tvItemName, "name"),
                        Pair(tvItemDescription, "description"),
                        Pair(tvItemDate, "date")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
        }

        private fun formatDate(isoString: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

            val date = inputFormat.parse(isoString)
            return outputFormat.format(date ?: Date())
        }
    }

}