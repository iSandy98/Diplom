package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_audio")
data class OfflineAudioEntity(

    @PrimaryKey
    val pointId:Int,

    val audioUrl:String?,

    val duration:Int?
)