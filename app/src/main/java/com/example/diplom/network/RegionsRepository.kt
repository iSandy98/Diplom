package com.example.diplom.network

import android.content.Context

class RegionsRepository(
    context: Context
) {
    private val api = RetrofitInstance.create(context)

    suspend fun getRegions(): Result<List<RegionDto>> {

        return runCatching {

            val allRegions =
                mutableListOf<RegionDto>()

            var page = 1

            while(true){

                val response =

                    api.getRegions(
                        page
                    )

                allRegions.addAll(
                    response.results
                )

                if(
                    response.next == null
                ) break

                page++
            }

            allRegions
        }
    }

    suspend fun getRegionDetail(id: Int): Result<RegionDto> {
        return runCatching {
            api.getRegionDetail(id)
        }
    }
}