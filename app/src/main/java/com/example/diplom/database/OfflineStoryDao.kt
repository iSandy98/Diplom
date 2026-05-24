package com.example.diplom.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineStoryDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun saveStories(
        stories:List<OfflineStoryEntity>
    )

    @Query(
        "SELECT * FROM offline_stories WHERE id=:id"
    )
    suspend fun getStory(
        id:Int
    ): OfflineStoryEntity?

    @Query(
        "DELETE FROM offline_stories WHERE regionName=:regionName"
    )
    suspend fun deleteStories(
        regionName:String
    )

    @Query(
        "SELECT * FROM offline_stories WHERE regionName=:regionName"
    )
    suspend fun getStories(
        regionName:String
    ): List<OfflineStoryEntity>
}