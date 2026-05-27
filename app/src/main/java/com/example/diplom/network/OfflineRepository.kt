package com.example.diplom.network

import android.content.Context
import com.example.diplom.database.DatabaseProvider
import com.example.diplom.database.OfflinePlaceEntity
import com.example.diplom.database.OfflineStoryEntity
import java.io.File
import com.example.diplom.database.OfflineRegionEntity
import com.example.diplom.database.OfflineAudioEntity
import com.example.diplom.database.RouteEntity
import com.example.diplom.database.RoutePointEntity

class OfflineRepository(
    context: Context
) {

    private val database =
        DatabaseProvider
            .getDatabase(context)

    private val dao =
        database
            .offlinePlaceDao()

    private val imageDownloader =
        ImageDownloader(context)

    private val storyDao =
        database.offlineStoryDao()

    private val photoDao =
        database.offlinePhotoDao()

    private val audioDao =
        database.offlineAudioDao()
    private val regionDao =
        database
            .offlineRegionDao()

    private val routeDao =
        database.routeDao()

    suspend fun getRoutes() =
        routeDao.getRoutes()

    suspend fun getRoutePoints(
        routeId:Int
    )=
        routeDao.getRoutePoints(
            routeId
        )

    suspend fun saveRoute(
        route: RouteDetailDto
    ){

        routeDao.saveRoute(

            RouteEntity(

                id =
                    route.id,

                name =
                    route.name,

                description =
                    route.description,

                duration =
                    route.duration_minutes
            )
        )

        android.util.Log.d(
            "OFFLINE_ROUTE",
            "сохранили маршрут: ${route.name}"
        )

        routeDao.saveRoutePoints(

            route.route_points.map {

                RoutePointEntity(

                    routeId =
                        route.id,

                    pointId =
                        it.point_id,

                    name =
                        it.name ?: "Без названия",

                    latitude =
                        it.latitude?.toDoubleOrNull()
                            ?:0.0,

                    longitude =
                        it.longitude?.toDoubleOrNull()
                            ?:0.0,

                    order =
                        it.order
                )
            }
        )
    }

    suspend fun saveRegionPlaces(
        regionName: String,
        places: List<PointListDto>
    ) {

        val mapped =
            places.map {

                OfflinePlaceEntity(

                    id =
                        it.id,

                    name =
                        it.name,

                    description =
                        it.description,

                    category =
                        it.category_name,

                    regionName =
                        regionName,

                    latitude =
                        it.latitude,

                    longitude =
                        it.longitude,

                    coverPhoto =
                        it.cover_photo,

                    hasAudio =
                        it.has_audio ?: false
                )
            }

        dao.savePlaces(
            mapped
        )
    }

    suspend fun getRegionPlaces(
        regionName:String
    )=
        dao.getPlacesByRegion(
            regionName
        )

    suspend fun deleteRegion(
        regionName:String
    ){

        val places =
            dao.getPlacesByRegion(
                regionName
            )

        places.forEach {

            val photos =

                photoDao.getPhotos(
                    it.id
                )

            photos.forEach { photo ->

                photo.image
                    ?.let { path ->

                        File(path)
                            .delete()
                    }
            }

            it.coverPhoto
                ?.let { path ->

                    File(path)
                        .delete()
                }

            photoDao.deletePhotos(
                it.id
            )

            val audio =

                audioDao.getAudio(
                    it.id
                )

            audio?.audioUrl
                ?.let { path ->

                    File(path)
                        .delete()
                }

            audioDao.deleteAudio(
                it.id
            )
        }

        storyDao.deleteStories(
            regionName
        )

        dao.deleteRegion(
            regionName
        )
    }

    suspend fun saveRegions(
        regions: List<RegionDto>
    ){

        regionDao.saveRegions(

            regions.map {

                val oldRegion =

                    regionDao
                        .getRegions()
                        .firstOrNull {
                                old ->
                            old.id == it.id
                        }

                        OfflineRegionEntity(

                    id = it.id,

                    name = it.name,

                    description =
                        it.description,

                    image =
                        it.image,

                    isDownloaded =
                        oldRegion?.isDownloaded
                            ?: false
                )
            }
        )
    }

    suspend fun getRegions()=

        regionDao
            .getRegions()


    suspend fun getDownloadedIds():

            Set<Int>{

        return getDownloadedRegions()

            .map {
                it.id
            }

            .toSet()
    }

    suspend fun saveStories(

        regionName:String,

        stories: List<StoryDto>

    ){

        storyDao.saveStories(

            stories.map {

                OfflineStoryEntity(

                    id =
                        it.id,

                    title =
                        it.title,

                    description =
                        it.description,

                    image =
                        it.image,

                    regionName =
                        regionName,

                    createdAt =
                        it.created_at
                )
            }
        )
    }


    suspend fun getStories(

        regionName:String

    )=

        storyDao.getStories(
            regionName
        )


    suspend fun savePhotos(

        pointId:Int,

        photos:List<PhotoDto>

    ){

        photoDao.savePhotos(

            photos.map {

                com.example.diplom.database
                    .OfflinePhotoEntity(

                        id =
                            it.id,

                        pointId =
                            pointId,

                        image =
                            it.image,

                        caption =
                            it.caption
                    )
            }
        )
    }


    suspend fun getPhotos(
        pointId:Int
    )=
        photoDao.getPhotos(
            pointId
        )


    suspend fun saveAudio(

        pointId:Int,

        audio:AudioGuideDto?

    ){

        if(audio==null)
            return

        val localAudio =

            audio.audio_file?.let {

                imageDownloader
                    .downloadImage(

                        url =
                            if(
                                it.startsWith("http")
                            )

                                it

                            else

                                "https://yave4en.pythonanywhere.com$it",

                        fileName =
                            "audio_${pointId}.mp3"
                    )
            }

        audioDao.saveAudio(

            OfflineAudioEntity(

                pointId =
                    pointId,

                audioUrl =
                    localAudio,

                duration =
                    audio.duration_seconds
            )
        )
    }


    suspend fun getAudio(
        pointId:Int
    )=
        audioDao.getAudio(
            pointId
        )


    suspend fun getRegionSize(
        regionName:String
    ):Float{

        val places =
            dao.getPlacesByRegion(
                regionName
            )

        val stories =
            storyDao.getStories(
                regionName
            )

        var size = 0

        size += places.size * 150
        size += stories.size * 80

        places.forEach {

            val photos =
                photoDao.getPhotos(
                    it.id
                )

            photos.forEach { photo ->

                photo.image
                    ?.let { path ->

                        val file =
                            File(path)

                        if(file.exists()){

                            size +=
                                file.length()
                                    .toInt()
                        }
                    }
            }

            it.coverPhoto
                ?.let { path ->

                    val file =
                        File(path)

                    if(file.exists()){

                        size +=
                            file.length()
                                .toInt()
                    }
                }

            val audio =
                audioDao.getAudio(
                    it.id
                )

            audio?.audioUrl
                ?.let { path ->

                    val file =
                        File(path)

                    if(file.exists()){

                        size +=
                            file.length()
                                .toInt()
                    }
                }
        }

        return size.toFloat() /
                (1024f*1024f)
    }

    suspend fun getAllPlaces() =

        dao.getAllPlaces()


    suspend fun getDownloadedRegions()=

        regionDao
            .getDownloadedRegions()
}


