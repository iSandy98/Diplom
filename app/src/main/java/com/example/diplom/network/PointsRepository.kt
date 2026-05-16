package com.example.diplom.network

import android.content.Context

class PointsRepository(
    context: Context
) {
    private val api = RetrofitInstance.create(context)

    suspend fun getPoints(): Result<List<PointListDto>> {
        return runCatching {
            api.getPoints().results
        }
    }

    suspend fun getPointDetail(id: Int): Result<PointDetailDto> {
        return runCatching {
            api.getPointDetail(id)
        }
    }
}