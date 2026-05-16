package com.example.diplom.network

import android.content.Context

class RegionsRepository(
    context: Context
) {
    private val api = RetrofitInstance.create(context)

    suspend fun getRegions(): Result<List<RegionDto>> {
        return runCatching {
            api.getRegions().results
        }
    }

    suspend fun getRegionDetail(id: Int): Result<RegionDto> {
        return runCatching {
            api.getRegionDetail(id)
        }
    }
}