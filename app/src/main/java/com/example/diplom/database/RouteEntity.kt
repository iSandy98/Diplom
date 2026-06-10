package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_routes"
)
data class RouteEntity(

    @PrimaryKey
    val id:Int,

    val name:String,

    val description:String?,

    val duration:Int?,

    val regionName:String = ""
)

