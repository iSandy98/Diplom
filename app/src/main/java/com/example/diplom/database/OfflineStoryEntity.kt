package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_stories")
data class OfflineStoryEntity(

    @PrimaryKey
    val id:Int,

    val title:String,

    val description:String?,

    val image:String?,

    val regionName:String?
)