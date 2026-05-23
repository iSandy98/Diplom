package com.example.diplom.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(

    entities = [

        OfflinePlaceEntity::class,

        OfflineRegionEntity::class,

        OfflineStoryEntity::class,

        OfflinePhotoEntity::class,

        OfflineAudioEntity::class

    ],

    version = 3
)

abstract class AppDatabase :
    RoomDatabase() {

    abstract fun
            offlinePlaceDao():
            OfflinePlaceDao

    abstract fun
            offlineRegionDao():
            OfflineRegionDao

    abstract fun
            offlineStoryDao():
            OfflineStoryDao

    abstract fun
            offlinePhotoDao():
            OfflinePhotoDao

    abstract fun
            offlineAudioDao():
            OfflineAudioDao
}