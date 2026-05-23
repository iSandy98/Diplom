package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_photos")
data class OfflinePhotoEntity(

    @PrimaryKey
    val id:Int,

    val pointId:Int,

    val image:String?,

    val caption:String?
)