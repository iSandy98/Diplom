package com.example.diplom.network

import android.content.Context
import com.example.diplom.database.DatabaseProvider
import com.example.diplom.database.OfflinePlaceEntity


class OfflineRepository(
    context: Context
) {

    private val database =
        DatabaseProvider
            .getDatabase(context)

    private val dao =
        database
            .offlinePlaceDao()

    private val storyDao =
        database.offlineStoryDao()

    private val photoDao =
        database.offlinePhotoDao()

    private val audioDao =
        database.offlineAudioDao()
    private val regionDao =
        database
            .offlineRegionDao()

    suspend fun saveRegionPlaces(
        regionName: String,
        places: List<PointListDto>
    ) {

        val mapped =
            places.map {

                OfflinePlaceEntity(

                    id = it.id,

                    name = it.name,

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
        dao.deleteRegion(
            regionName
        )
    }

    suspend fun saveRegions(
        regions: List<RegionDto>
    ){

        regionDao.saveRegions(

            regions.map {

                com.example.diplom.database
                    .OfflineRegionEntity(

                        id =
                            it.id,

                        name =
                            it.name,

                        description =
                            it.description,

                        image =
                            it.image
                    )
            }
        )
    }

    suspend fun getRegions()=

        regionDao
            .getRegions()


    suspend fun getDownloadedIds():

            Set<Int>{

        return dao
            .getAllRegionNames()
            .mapNotNull { regionName ->

                regionDao
                    .getRegions()
                    .firstOrNull {

                        it.name ==
                                regionName
                    }?.id
            }
            .toSet()
    }

    suspend fun saveStories(

        regionName:String,

        stories: List<StoryDto>

    ){

        storyDao.saveStories(

            stories.map {

                com.example.diplom.database
                    .OfflineStoryEntity(

                        id =
                            it.id,

                        title =
                            it.title,

                        description =
                            it.description,

                        image =
                            it.image,

                        regionName =
                            regionName
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

        audioDao.saveAudio(

            com.example.diplom.database
                .OfflineAudioEntity(

                    pointId =
                        pointId,

                    audioUrl =
                        audio.audio_file,

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
}


