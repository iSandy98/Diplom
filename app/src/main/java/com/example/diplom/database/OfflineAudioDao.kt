package com.example.diplom.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineAudioDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun saveAudio(
        audio: OfflineAudioEntity
    )

    @Query(
        "SELECT * FROM offline_audio WHERE pointId=:pointId"
    )
    suspend fun getAudio(
        pointId:Int
    ): OfflineAudioEntity?

    @Query(
        "DELETE FROM offline_audio WHERE pointId=:pointId"
    )
    suspend fun deleteAudio(
        pointId:Int
    )
}
