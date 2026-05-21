package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_places"
)
data class OfflinePlaceEntity(

    @PrimaryKey
    val id: Int,

    val name: String,

    val category: String?,

    val regionName: String?,

    val latitude: Double?,

    val longitude: Double?,

    val coverPhoto: String?,

    val hasAudio: Boolean
)