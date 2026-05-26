package com.example.diplom.network

import android.content.Context

class PointsRepository(
    context: Context
) {
    private val api = RetrofitInstance.create(context)

    suspend fun getPoints():
            Result<List<PointListDto>> {

        return runCatching {

            val allPoints =
                mutableListOf<PointListDto>()

            var page = 1

            while(true){

                val response =

                    api.getPoints(
                        page
                    )

                allPoints.addAll(
                    response.results
                )

                if(
                    response.next == null
                ){

                    break
                }

                page++
            }

            android.util.Log.d(

                "POINT_COUNT",

                "загружено=${allPoints.size}"
            )

            allPoints
        }
    }

    suspend fun getPointDetail(id: Int): Result<PointDetailDto> {
        return runCatching {
            api.getPointDetail(id)
        }
    }

    suspend fun getRoutes():
            Result<List<RouteDetailDto>> {

        return runCatching {

            val routes =
                api.getRoutes().results

            routes.forEach {

                android.util.Log.d(
                    "ROUTE_TEST",

                    "${it.name} | " +
                            "${it.points_count} точек | " +
                            "${it.duration_minutes} мин"
                )
            }

            routes
        }
    }

    suspend fun getRouteDetail(
        id:Int
    ):Result<RouteDetailDto>{

        return runCatching {

            api.getRouteDetail(id)
        }
    }
}