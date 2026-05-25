package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName="offline_regions"
)
data class OfflineRegionEntity(

    @PrimaryKey
    val id:Int,

    val name:String,

    val description:String?,

    val image:String?,

    val isDownloaded:Boolean=false
)
