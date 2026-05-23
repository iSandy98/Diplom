package com.example.diplom.database

import androidx.room.*

@Dao
interface OfflineRegionDao{

    @Insert(
        onConflict=
            OnConflictStrategy.REPLACE
    )
    suspend fun saveRegions(
        regions:
        List<OfflineRegionEntity>
    )

    @Query(
        "SELECT * FROM offline_regions"
    )
    suspend fun getRegions():
            List<OfflineRegionEntity>
}