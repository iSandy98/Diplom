package com.example.diplom.network

import android.content.Context

class OfflineRegionRepository(
    context: Context
) {

    private val api =
        RetrofitInstance.create(context)

    suspend fun getOfflineRegion(
        id:Int
    ): Result<RegionOfflineResponse> {

        return runCatching {

            api.getOfflineRegion(id)

        }
    }
}