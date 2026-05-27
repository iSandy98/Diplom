package com.example.diplom.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_route_points"
)
data class RoutePointEntity(

    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,

    val routeId:Int,

    val pointId:Int,

    val name:String?,

    val latitude:Double,

    val longitude:Double,

    val order:Int
)