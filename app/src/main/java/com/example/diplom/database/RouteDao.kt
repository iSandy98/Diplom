package com.example.diplom.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RouteDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun saveRoute(
        route: RouteEntity
    )

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun saveRoutePoints(
        points: List<RoutePointEntity>
    )

    @Query(
        "SELECT * FROM offline_routes"
    )
    suspend fun getRoutes():
            List<RouteEntity>

    @Query(
        """
        SELECT * FROM offline_route_points
        WHERE routeId=:routeId
        ORDER BY `order`
        """
    )
    suspend fun getRoutePoints(
        routeId:Int
    ): List<RoutePointEntity>

    @Query(
        "SELECT * FROM offline_routes WHERE regionName=:regionName"
    )
    suspend fun getRoutesByRegion(
        regionName: String
    ): List<RouteEntity>

    @Query(
        "DELETE FROM offline_route_points WHERE routeId=:routeId"
    )
    suspend fun deleteRoutePoints(
        routeId: Int
    )

    @Query(
        "DELETE FROM offline_routes WHERE regionName=:regionName"
    )
    suspend fun deleteRoutesByRegion(
        regionName: String
    )
}