package com.example.diplom.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        OfflinePlaceEntity::class
    ],
    version = 1
)

abstract class AppDatabase:
    RoomDatabase(){

    abstract fun
            offlinePlaceDao():
            OfflinePlaceDao
}