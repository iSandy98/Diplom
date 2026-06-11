package com.example.diplom.ui.theme.screens

import kotlinx.coroutines.async
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplom.network.PointsRepository
import com.example.diplom.network.RegionsRepository
import com.example.diplom.network.StoriesRepository
import com.example.diplom.utils.buildImageUrl
import com.example.diplom.network.OfflineRepository
import androidx.compose.ui.text.style.TextOverflow
import com.example.diplom.utils.CurrentRegionManager

@Composable
fun HomeScreen(
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
    val storiesRepository = remember { StoriesRepository(context) }
    val offlineRepository =
        remember {
            OfflineRepository(context)
        }

    val currentRegionManager =

        remember{

            CurrentRegionManager(
                context
            )
        }

    var currentRegion by remember {

        mutableStateOf(

            currentRegionManager
                .getRegion()

                .replace(
                    "Город ",
                    ""
                )

                .trim()
        )
    }

    var districts by remember { mutableStateOf<List<DistrictUi>>(emptyList()) }
    var stories by remember { mutableStateOf<List<StoryUi>>(emptyList()) }
    var places by remember { mutableStateOf<List<PlaceUi>>(emptyList()) }

    var isDistrictsLoading by remember { mutableStateOf(true) }
    var isStoriesLoading by remember { mutableStateOf(true) }
    var isPlacesLoading by remember { mutableStateOf(true) }

    var displayRegionTitle by remember { mutableStateOf(currentRegion) }
    var displayDistricts by remember { mutableStateOf<List<DistrictUi>>(emptyList()) }
    var displayStories by remember { mutableStateOf<List<StoryUi>>(emptyList()) }
    var displayPlaces by remember { mutableStateOf<List<PlaceUi>>(emptyList()) }

    LaunchedEffect(currentRegion) {
        val regionsDeferred = async { regionsRepository.getRegions() }
        val storiesDeferred = async { storiesRepository.getStories() }
        val pointsDeferred = async { pointsRepository.getPoints() }

        val regionsResult = regionsDeferred.await()
        val storiesResult = storiesDeferred.await()
        val pointsResult = pointsDeferred.await()

        regionsResult
            .onSuccess { data ->

                districts =
                    data
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
            }

            .onFailure {

                val offlineRegions =

                    offlineRepository
                        .getDownloadedRegions()

                districts =
                    offlineRegions
                        .map {

                            DistrictUi(

                                id =
                                    it.id.toString(),

                                title =
                                    it.name,

                                imageUrl =

                                    if(
                                        it.image
                                            ?.startsWith("/data/")
                                        == true
                                    )

                                        "file://${it.image}"

                                    else

                                        buildImageUrl(
                                            it.image
                                        )
                            )
                        }
            }
        isDistrictsLoading = false

        storiesResult
            .onSuccess { data ->

                stories =
                    data
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

                // Save for offline without blocking display and without downloading images
                offlineRepository.saveStories(
                    "all",
                    data,
                    downloadImages = false
                )
            }

            .onFailure {

                val offlineStories =

                    offlineRepository
                        .getStories(
                            "all"
                        )

                stories =
                    offlineStories
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
                                    if (it.image?.startsWith("/data/") == true)
                                        "file://${it.image}"
                                    else
                                        buildImageUrl(it.image)
                            )
                        }
            }

        isStoriesLoading = false

        pointsResult
            .onSuccess { data ->

                val regionPlaces =

                    data.filter {

                        it.region_name
                            ?.contains(
                                currentRegion,
                                ignoreCase = true
                            ) == true
                    }

                val randomPlaces =

                    regionPlaces
                        .shuffled()
                        .take(6)

                places =
                    randomPlaces
                        .map {

                            PlaceUi(

                                id =
                                    it.id.toString(),

                                title =
                                    it.name,

                                description =
                                    it.category_name
                                        ?: "Описание",

                                locationLabel =
                                    buildString {

                                        append(
                                            it.region_name
                                                ?: ""
                                        )

                                        if(
                                            it.has_audio==true
                                        ){
                                            append(
                                                " • Аудиогид"
                                            )
                                        }
                                    },

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

            .onFailure {

                val offlinePlaces =

                    offlineRepository
                        .getRegionPlaces(
                            currentRegion
                        )

                places =
                    offlinePlaces
                        .shuffled()
                        .take(6)
                        .map {

                            PlaceUi(

                                id =
                                    it.id.toString(),

                                title =
                                    it.name,

                                description =
                                    it.description
                                        ?: it.category
                                        ?: "Описание",

                                locationLabel =
                                    it.regionName ?: "",

                                imageUrl =

                                    if(
                                        it.coverPhoto
                                            ?.startsWith("/data/")
                                        == true
                                    )

                                        "file://${it.coverPhoto}"

                                    else

                                        buildImageUrl(
                                            it.coverPhoto
                                        ),

                                latitude =
                                    it.latitude,

                                longitude =
                                    it.longitude
                            )
                        }
            }

        isPlacesLoading = false
    }

    LaunchedEffect(com.example.diplom.utils.LanguageState.isEnglish, districts, stories, places, currentRegion) {
        if (com.example.diplom.utils.LanguageState.isEnglish) {
            displayRegionTitle = com.example.diplom.utils.TranslationHelper.translate(currentRegion)
            displayDistricts = districts.map { it.copy(title = com.example.diplom.utils.TranslationHelper.translate(it.title)) }
            displayStories = stories.map { it.copy(title = com.example.diplom.utils.TranslationHelper.translate(it.title), subtitle = com.example.diplom.utils.TranslationHelper.translate(it.subtitle)) }
            displayPlaces = places.map { it.copy(title = com.example.diplom.utils.TranslationHelper.translate(it.title), locationLabel = com.example.diplom.utils.TranslationHelper.translate(it.locationLabel)) }
        } else {
            displayRegionTitle = currentRegion
            displayDistricts = districts
            displayStories = stories
            displayPlaces = places
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            HomeTopBar(

                title =
                    displayRegionTitle,

                onOpenProfile =
                    onOpenProfile
            )
        }

        item {
            SectionHeader(
                title = if (com.example.diplom.utils.LanguageState.isEnglish) "Districts of Yakutia" else "Районы Якутии",
                onClick = onOpenDistrictsList
            )
        }

        item {
            if (isDistrictsLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    items(displayDistricts) { district ->
                        DistrictItem(
                            item = district,
                            onClick = { onOpenDistrict(district.id) }
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = if (com.example.diplom.utils.LanguageState.isEnglish) "Near Me" else "Рядом со мной",
                onClick = onOpenMap
            )
        }

        item {
            NearbyMapPreview(
                places = displayPlaces,
                onClick = onOpenMap
            )
        }

        item {
            SectionHeader(
                title = if (com.example.diplom.utils.LanguageState.isEnglish) "History in Photos" else "История на фотографиях",
                onClick = onOpenStories
            )
        }

        item {
            if (isStoriesLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayStories) { story ->
                            Box(
                                modifier = Modifier.clickable { onOpenStories() }
                            ) {
                                StoryItem(item = story)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            SectionHeader(
                title = if (com.example.diplom.utils.LanguageState.isEnglish) "Places of Interest" else "Интересные места",
                onClick = {},
                showArrow = false
            )
        }

        if (isPlacesLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(displayPlaces) { place ->
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
fun HomeTopBar(

    title:String,

    onOpenProfile:()->Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenProfile) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Профиль",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF1F1F1F)
                )
            }
            androidx.compose.material3.TextButton(
                onClick = {
                    com.example.diplom.utils.LanguageState.isEnglish =
                        !com.example.diplom.utils.LanguageState.isEnglish
                }
            ) {
                androidx.compose.material3.Text(
                    text = if (com.example.diplom.utils.LanguageState.isEnglish) "RU" else "EN",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF49454F)
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

@Composable
fun SectionHeader(
    title: String,
    onClick: () -> Unit,
    showArrow:Boolean=true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 28.sp
            ),
            color = Color(0xFF1F1F1F)
        )

        if(showArrow){

            Text(
                text = "→",

                modifier =
                    Modifier.clickable {
                        onClick()
                    },

                style =
                    TextStyle(
                        fontSize = 24.sp
                    ),

                color =
                    Color(0xFF1F1F1F)
            )
        }
    }
}

@Composable
fun DistrictItem(
    item: DistrictUi,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE7E3EB))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = TextStyle(fontSize = 14.sp),
            color = Color(0xFF1F1F1F),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun StoryItem(
    item: StoryUi
) {
    Column(
        modifier = Modifier.width(132.dp)
    ) {
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(width = 132.dp, height = 179.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 132.dp, height = 179.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE7E3EB))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = TextStyle(fontSize = 14.sp),
            color = Color(0xFF1F1F1F),
            maxLines = 2
        )
    }
}

@Composable
fun PlaceItem(
    item: PlaceUi,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(120.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE7E3EB))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp)
        ) {
            Text(
                text = item.title,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.1.sp
                ),
                color = Color(0xFF1F1F1F)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text =
                    item.locationLabel,

                style = TextStyle(
                    fontSize = 14.sp
                ),

                color =
                    Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Локация",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF1F1F1F)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = item.locationLabel,
                    style = TextStyle(fontSize = 12.sp),
                    color = Color(0xFF49454F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}