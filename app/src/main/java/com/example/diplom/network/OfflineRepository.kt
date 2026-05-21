package com.example.diplom.network

import android.content.Context
import com.example.diplom.database.DatabaseProvider
import com.example.diplom.database.OfflinePlaceEntity


class OfflineRepository(
    context: Context
) {

    private val dao =
        DatabaseProvider
            .getDatabase(context)
            .offlinePlaceDao()

    suspend fun saveRegionPlaces(
        regionName: String,
        places: List<PointListDto>
    ) {

        val mapped =
            places.map {

                OfflinePlaceEntity(

                    id = it.id,

                    name = it.name,

                    category =
                        it.category_name,

                    regionName =
                        regionName,

                    latitude =
                        it.latitude,

                    longitude =
                        it.longitude,

                    coverPhoto =
                        it.cover_photo,

                    hasAudio =
                        it.has_audio ?: false
                )
            }

        dao.savePlaces(
            mapped
        )
    }

    suspend fun getRegionPlaces(
        regionName:String
    )=
        dao.getPlacesByRegion(
            regionName
        )

    suspend fun deleteRegion(
        regionName:String
    ){
        dao.deleteRegion(
            regionName
        )
    }
}
