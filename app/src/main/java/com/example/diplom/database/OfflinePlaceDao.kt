package com.example.diplom.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflinePlaceDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun savePlaces(
        places:
        List<OfflinePlaceEntity>
    )

    @Query(
        "SELECT * FROM offline_places WHERE regionName=:region"
    )
    suspend fun getPlacesByRegion(
        region:String
    ): List<OfflinePlaceEntity>

    @Query(
        "DELETE FROM offline_places WHERE regionName=:region"
    )
    suspend fun deleteRegion(
        region:String
    )

    @Query(
        "SELECT DISTINCT regionName FROM offline_places"
    )
    suspend fun getAllRegionNames():
            List<String>
}