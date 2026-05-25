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

    @Query(
        "UPDATE offline_regions SET isDownloaded=1 WHERE id=:id"
    )
    suspend fun markDownloaded(
        id:Int
    )

    @Query(
        "SELECT * FROM offline_regions WHERE isDownloaded=1"
    )
    suspend fun getDownloadedRegions():
            List<OfflineRegionEntity>
}