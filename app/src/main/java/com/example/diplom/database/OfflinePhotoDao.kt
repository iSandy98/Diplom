package com.example.diplom.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflinePhotoDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun savePhotos(
        photos: List<OfflinePhotoEntity>
    )

    @Query(
        "SELECT * FROM offline_photos WHERE pointId=:pointId"
    )
    suspend fun getPhotos(
        pointId:Int
    ): List<OfflinePhotoEntity>

    @Query(
        "DELETE FROM offline_photos WHERE pointId=:pointId"
    )
    suspend fun deletePhotos(
        pointId:Int
    )
}