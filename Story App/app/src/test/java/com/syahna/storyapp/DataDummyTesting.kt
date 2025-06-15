package com.syahna.storyapp

import com.syahna.storyapp.remote.response.ListStoryItem

object DummyDataTesting {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (index in 0..100) {
            val storyItem = ListStoryItem(
                photoUrl = "https://dummyimage.com/story_$index.jpg",
                createdAt = "2024-12-12T12:00:00Z",
                name = "USer $index",
                description = "Story number $index generated for testing purposes.",
                lon = 110.0 + index,
                id = "story_$index",
                lat = -9.0 - index
            )
            items.add(storyItem)
        }
        return items
    }
}