package com.example.diplom.network

import android.content.Context

class StoriesRepository(
    context: Context
) {
    private val api = RetrofitInstance.create(context)

    suspend fun getStories(): Result<List<StoryDto>> {
        return runCatching {
            api.getStories().results
        }
    }

    suspend fun getStoryDetail(id: Int): Result<StoryDto> {
        return runCatching {
            api.getStoryDetail(id)
        }
    }
}