package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.network.PointsRepository
import com.example.diplom.network.RegionDto
import com.example.diplom.network.RegionsRepository
import com.example.diplom.utils.buildImageUrl
import com.example.diplom.network.OfflineRepository
import com.example.diplom.network.StoriesRepository

@Composable
fun DistrictDetailScreen(
    districtId: String,
    onOpenDistrict: (String) -> Unit = {},
    onOpenDistrictsList: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onOpenStories: () -> Unit = {},
    onOpenPlace: (String) -> Unit = {},
    onPlayAudio: (String) -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val regionsRepository = remember { RegionsRepository(context) }
    val pointsRepository = remember { PointsRepository(context) }
    val offlineRepository = remember {
        OfflineRepository(context)
    }
    var district by remember { mutableStateOf<RegionDto?>(null) }
    var otherDistricts by remember { mutableStateOf<List<DistrictUi>>(emptyList()) }
    var places by remember { mutableStateOf<List<PlaceUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val storiesRepository =
        remember {
            StoriesRepository(
                context
            )
        }

    var stories by remember {
        mutableStateOf<List<StoryUi>>(
            emptyList()
        )
    }

    LaunchedEffect(districtId) {
        isLoading = true

        val id = districtId.toIntOrNull()
        val offlineRegions =
            offlineRepository
                .getRegions()

        val fallbackRegionName =

            offlineRegions
                .firstOrNull {

                    it.id.toString() ==
                            districtId
                }
                ?.name
                ?: ""

        if (id != null) {
            try {

                val pointsResult =
                    pointsRepository
                        .getPoints()

                if (pointsResult.isFailure) {

                    val districtName =
                        fallbackRegionName

                    android.util.Log.d(
                        "OFFLINE_CHECK",
                        "Ищу район=$districtName"
                    )

                    val offlinePlaces =
                        offlineRepository
                            .getRegionPlaces(
                                districtName
                            )

                    android.util.Log.d(
                        "OFFLINE_CHECK",
                        "Нашли=${offlinePlaces.size}"
                    )

                    places =
                        offlinePlaces.map {

                            PlaceUi(
                                id = it.id.toString(),
                                title = it.name,
                                description =
                                    it.category ?: "Описание",

                                locationLabel =
                                    it.regionName ?: "",

                                imageUrl =
                                    buildImageUrl(
                                        it.coverPhoto
                                    ),

                                latitude =
                                    it.latitude,

                                longitude =
                                    it.longitude
                            )
                        }

                    android.util.Log.d(
                        "OFFLINE_CHECK",
                        "placesUI=${places.size}"
                    )

                    district =
                        RegionDto(
                            id = id,
                            name = districtName,
                            description = "",
                            image = null,
                            points_count = null
                        )

                    isLoading = false

                    return@LaunchedEffect
                }

                val districtResult =
                    regionsRepository
                        .getRegionDetail(id)

                val allRegionsResult =
                    regionsRepository
                        .getRegions()

                val storiesResult =
                    storiesRepository
                        .getStories()

                stories =
                    storiesResult
                        .getOrDefault(
                            emptyList()
                        )
                        .take(5)
                        .map {

                            StoryUi(
                                id =
                                    it.id.toString(),

                                title =
                                    it.title,

                                subtitle =
                                    it.description ?: "",

                                imageUrl =
                                    buildImageUrl(
                                        it.image
                                    )
                            )
                        }

                district =
                    districtResult.getOrNull()

                val currentDistrictName =

                    district?.name
                        ?.replace(
                            "Город ",
                            ""
                        )
                        ?.trim()

                otherDistricts =
                    allRegionsResult
                        .getOrDefault(
                            emptyList()
                        )
                        .filter {
                            it.id != id
                        }
                        .take(5)
                        .map {

                            DistrictUi(

                                id =
                                    it.id.toString(),

                                title =
                                    it.name,

                                imageUrl =
                                    buildImageUrl(
                                        it.image
                                    )
                            )
                        }

                val onlinePlaces =

                    pointsResult
                        .getOrDefault(
                            emptyList()
                        )
                        .filter {

                            it.region_name
                                ?.trim()
                                ?.contains(
                                    currentDistrictName ?: "",
                                    ignoreCase = true
                                ) == true
                        }


                if(
                    onlinePlaces.isNotEmpty()
                ){

                    places =
                        onlinePlaces.map {

                            PlaceUi(
                                id = it.id.toString(),
                                title = it.name,
                                description =
                                    it.category_name
                                        ?: "Описание отсутствует",

                                locationLabel =
                                    it.region_name
                                        ?: "Локация",

                                imageUrl =
                                    buildImageUrl(
                                        it.cover_photo
                                    ),

                                latitude =
                                    it.latitude,

                                longitude =
                                    it.longitude
                            )
                        }
                }

            }catch(
                e: Exception
            ){

                val districtName =

                    district?.name
                        ?.replace(
                            "Город ",
                            ""
                        )
                        ?.trim()

                        ?: fallbackRegionName

                android.util.Log.d(
                    "OFFLINE_CHECK",
                    "Ищу район=$districtName"
                )

                val offlinePlaces =

                    offlineRepository
                        .getRegionPlaces(
                            districtName
                        )

                places =
                    offlinePlaces.map {

                        PlaceUi(

                            id =
                                it.id.toString(),

                            title =
                                it.name,

                            description =
                                it.category
                                    ?: "Описание",

                            locationLabel =
                                it.regionName ?: "",

                            imageUrl =
                                buildImageUrl(
                                    it.coverPhoto
                                ),

                            latitude =
                                it.latitude,

                            longitude =
                                it.longitude
                        )
                    }

                android.util.Log.d(
                    "OFFLINE_CHECK",
                    "placesUI=${places.size}"
                )

                android.util.Log.d(
                    "OFFLINE_CHECK",
                    "Нашли: ${offlinePlaces.size}"
                )
            }
        }

        android.util.Log.d(
            "OFFLINE_CHECK",
            "FINAL=${places.size}"
        )

        isLoading = false
    }

    if (isLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            DistrictTopBar(
                title = district?.name ?: "Район",
                onOpenProfile = onOpenProfile
            )
        }

        item {
            SectionHeader(
                title = "Другие районы Якутии",
                onClick = onOpenDistrictsList
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                items(otherDistricts) { item ->
                    DistrictItem(
                        item = item,
                        onClick = { onOpenDistrict(item.id) }
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Рядом со мной",
                onClick = onOpenMap
            )
        }

        item {

            Text(
                text = "Точек: ${places.size}",
                color = Color.Red
            )
        }

        item {
            NearbyMapPreview(
                places = places,
                onClick = onOpenMap
            )
        }

        item {
            SectionHeader(
                title = "История на фотографиях",
                onClick = onOpenStories
            )
        }

        item {
            androidx.compose.foundation.layout.Column {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(stories) { story ->
                        StoryItem(item = story)
                    }
                }

                Spacer(Modifier.height(12.dp))

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .background(
                                    if (index == 1) Color(0xFF1F1F1F)
                                    else Color(0xFFD9D9D9),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Интересные места",
                onClick = {}
            )
        }

        if (places.isEmpty()) {
            item {
                Text(
                    text = "В этом районе пока нет достопримечательностей",
                    color = Color(0xFF49454F)
                )
            }
        } else {
            items(places) { place ->
                PlaceItem(
                    item = place,
                    onClick = { onOpenPlace(place.id) },
                    onPlayClick = { onPlayAudio(place.id) }
                )
            }
        }
    }
}

@Composable
private fun DistrictTopBar(
    title: String,
    onOpenProfile: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenProfile) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Профиль",
                    tint = Color(0xFF1F1F1F)
                )
            }
        }

        Text(
            text = title,
            style = TextStyle(
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            color = Color(0xFF1F1F1F)
        )
    }
}