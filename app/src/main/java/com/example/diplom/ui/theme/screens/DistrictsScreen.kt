package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplom.network.RegionDto
import com.example.diplom.network.RegionsRepository
import androidx.compose.material.icons.outlined.Delete
import com.example.diplom.network.PointsRepository
import com.example.diplom.network.OfflineRepository
import kotlinx.coroutines.launch
import com.example.diplom.utils.buildImageUrl
import com.example.diplom.network.OfflineRegionRepository
import com.example.diplom.network.ImageDownloader
import com.example.diplom.database.DatabaseProvider
import com.example.diplom.database.OfflineStoryEntity

private const val API_DOMAIN =
    "https://yave4en.pythonanywhere.com"

@Composable
fun DistrictsScreen(
    onBack: () -> Unit,
    onOpenDistrict: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { RegionsRepository(context) }

    val offlineBundleRepository =
        remember {

            OfflineRegionRepository(
                context
            )
        }

    val offlineRepository = remember {
        OfflineRepository(context)
    }

    val database =
        remember {
            DatabaseProvider
                .getDatabase(context)
        }

    val imageDownloader =

        remember{

            ImageDownloader(
                context
            )
        }

    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var selectedTab by remember {

        mutableIntStateOf(0)
    }
    var regions by remember { mutableStateOf<List<RegionDto>>(emptyList()) }
    var downloadedIds by remember {

        mutableStateOf(
            setOf<Int>()
        )
    }
    var downloadedRegions by remember {

        mutableStateOf<List<RegionDto>>(
            emptyList()
        )
    }

    var regionSizes by remember {

        mutableStateOf(
            mapOf<Int, Float>()
        )
    }

    var downloadingRegionId by remember {

        mutableStateOf<Int?>(null)
    }

    var downloadProgress by remember {

        mutableFloatStateOf(0f)
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {

        try {

            val result =
                repository
                    .getRegions()

            result.onSuccess {

                regions = it

                it.forEach {

                    android.util.Log.d(
                        "REGION_TEST",
                        "id=${it.id} name=${it.name}"
                    )
                }

                scope.launch {

                    offlineRepository
                        .saveRegions(it)

                    downloadedIds =

                        offlineRepository
                            .getDownloadedIds()

                    downloadedRegions =

                        offlineRepository
                            .getDownloadedRegions()

                            .map {

                                RegionDto(

                                    id = it.id,

                                    name = it.name,

                                    description =
                                        it.description,

                                    image =
                                        it.image,

                                    points_count =
                                        null
                                )
                            }

                    val sizes = mutableMapOf<Int, Float>()

                    downloadedIds.forEach { id ->

                        val region =

                            regions.firstOrNull {
                                it.id == id
                            }

                        if(region != null){

                            sizes[id] =

                                offlineRepository
                                    .getRegionSize(
                                        region.name
                                    )
                        }
                    }

                    regionSizes = sizes
                }
            }

        } catch(
            e: Exception
        ){

            downloadedIds =

                offlineRepository
                    .getDownloadedIds()

            val offlineRegions =

                offlineRepository
                    .getRegions()

                    .filter {

                        downloadedIds
                            .contains(
                                it.id
                            )
                    }

            downloadedRegions =

                offlineRegions.map {

                    RegionDto(

                        id = it.id,

                        name = it.name,

                        description =
                            it.description,

                        image =
                            it.image,

                        points_count =
                            null
                    )
                }

            regions =
                downloadedRegions

            val sizes =
                mutableMapOf<Int,Float>()

            downloadedRegions
                .forEach { region ->

                    sizes[region.id] =

                        offlineRepository
                            .getRegionSize(
                                region.name
                            )
                }

            regionSizes =
                sizes

            errorText = null
        }

        isLoading = false
    }

    val source =

        if(selectedTab==1){

            downloadedRegions

        } else {

            regions
        }

    val filteredRegions =

        if(query.isBlank()){

            source

        } else {

            source.filter {

                it.name.contains(
                    query,
                    true
                )
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF1F1F1F)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Районы Якутии",
            style = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
            color = Color(0xFF1F1F1F),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(
                    "Введите название района",
                    color = Color(0xFF666666),
                    fontSize = 16.sp
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Поиск"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(12.dp))

        TabRow(
            selectedTabIndex =
                selectedTab,

            containerColor =
                Color.White,

            contentColor =
                Color(0xFF1F1F1F)
        ) {

            Tab(
                selected =
                    selectedTab==0,

                onClick = {
                    selectedTab=0
                },

                selectedContentColor =
                    Color(0xFF1F1F1F),

                unselectedContentColor =
                    Color(0xFF666666),

                text = {

                    Text(
                        "Все (${regions.size})"
                    )
                }
            )

            Tab(
                selected =
                    selectedTab==1,

                onClick = {
                    selectedTab=1
                },

                selectedContentColor =
                    Color(0xFF1F1F1F),

                unselectedContentColor =
                    Color(0xFF666666),

                text = {

                    Text(

                        "Скачанные (${downloadedIds.size})"
                    )
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorText != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorText ?: "Ошибка",
                        color = Color.Red
                    )
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredRegions) { region ->
                        DistrictRowFromApi(

                            region = region,

                            isDownloaded =
                                downloadedIds.contains(
                                    region.id
                                ),

                            isDownloading =
                                downloadingRegionId ==
                                        region.id,

                            progress =
                                downloadProgress,

                            sizeMb =
                                regionSizes[
                                    region.id
                                ] ?: 0f,

                            onOpen = {

                                onOpenDistrict(
                                    region.id.toString()
                                )
                            },

                            onToggleDownload = {

                                if(
                                    downloadingRegionId != null
                                ) return@DistrictRowFromApi

                                downloadingRegionId =
                                    region.id

                                downloadProgress = 0f

                                scope.launch {
                                    val result =

                                        offlineBundleRepository
                                            .getOfflineRegion(
                                                region.id
                                            )

                                    result.onSuccess { bundle ->


                                        offlineRepository.saveRegions(
                                            listOf(bundle.region)
                                        )

                                        database
                                            .offlineRegionDao()
                                            .markDownloaded(
                                                region.id
                                            )


                                        offlineRepository.saveStories(
                                            bundle.region.name,
                                            bundle.stories
                                        )

                                        bundle.routes.forEach {

                                            offlineRepository
                                                .saveRoute(it)
                                        }

                                        offlineRepository.saveRegionPlaces(
                                            bundle.region.name,
                                            bundle.points.map { point ->
                                                val originalUrl =
                                                    point.photos
                                                        .firstOrNull()
                                                        ?.url

                                                android.util.Log.d(
                                                    "COVER_TEST",
                                                    "url=$originalUrl"
                                                )

                                                val localCover =

                                                    originalUrl?.let { photoUrl ->

                                                        val downloaded =

                                                            imageDownloader
                                                                .downloadImage(

                                                                    url =
                                                                        buildImageUrl(photoUrl)
                                                                            ?: "",

                                                                    fileName =
                                                                        "cover_${point.id}.jpg"
                                                                )

                                                        android.util.Log.d(
                                                            "COVER_TEST",
                                                            "saved=$downloaded"
                                                        )

                                                        downloaded
                                                    }

                                                com.example.diplom.network.PointListDto(

                                                    id = point.id,

                                                    name = point.name,

                                                    category_name = point.category_slug,

                                                    region_name = bundle.region.name,

                                                    latitude = point.latitude,

                                                    longitude = point.longitude,

                                                    cover_photo = localCover,

                                                    has_audio = point.audio != null,

                                                    description = point.description,

                                                    created_at = point.updated_at
                                                )
                                            }
                                        )

                                        val totalPoints =
                                            bundle.points.size

                                        var currentPoint = 0

                                        bundle.points.forEach { point ->

                                            android.util.Log.d(
                                                "AUDIO_TEST",
                                                "point=${point.name} audio=${point.audio?.audio_file}"
                                            )

                                            offlineRepository.savePhotos(
                                                pointId = point.id,
                                                photos = point.photos.map { photo ->

                                                    val localPath =

                                                        photo.url?.let {

                                                            imageDownloader
                                                                .downloadImage(

                                                                    url =
                                                                        buildImageUrl(it)
                                                                            ?: "",

                                                                    fileName =
                                                                        "photo_${photo.id}.jpg"
                                                                )
                                                        }

                                                    com.example.diplom.network.PhotoDto(

                                                        id =
                                                            photo.id,

                                                        image =
                                                            localPath,

                                                        caption =
                                                            photo.caption,

                                                        order =
                                                            photo.order
                                                    )
                                                }
                                            )

                                            database
                                                .offlineStoryDao()
                                                .saveStories(

                                                    bundle.stories.map {

                                                        OfflineStoryEntity(

                                                            id = it.id,

                                                            title = it.title,

                                                            description = it.description,

                                                            image = it.image,

                                                            regionName =
                                                                bundle.region.name,

                                                            createdAt =
                                                                it.created_at
                                                        )
                                                    }
                                                )


                                            offlineRepository.saveAudio(
                                                pointId = point.id,
                                                audio = point.audio
                                            )

                                            currentPoint++

                                            downloadProgress =

                                                if(totalPoints>0)

                                                    currentPoint.toFloat() /
                                                            totalPoints

                                                else 1f
                                        }

                                        downloadedIds =
                                            downloadedIds + region.id

                                        downloadedRegions =

                                            offlineRepository
                                                .getDownloadedRegions()

                                                .map {

                                                    RegionDto(

                                                        id = it.id,

                                                        name = it.name,

                                                        description =
                                                            it.description,

                                                        image =
                                                            it.image,

                                                        points_count =
                                                            null
                                                    )
                                                }

                                        regions = regions.map {

                                            if(it.id == region.id){

                                                it.copy()

                                            } else it
                                        }

                                        val size: Float =

                                            offlineRepository
                                                .getRegionSize(
                                                    bundle.region.name
                                                )

                                        regionSizes =
                                            regionSizes + mapOf(
                                                region.id to size
                                            )

                                        downloadProgress = 1f


                                        downloadingRegionId =
                                            null
                                    }
                                    result.onFailure {

                                        downloadingRegionId = null

                                        downloadProgress = 0f
                                    }

                                }
                            },

                            onDelete = {

                                downloadedIds =
                                    downloadedIds -
                                            region.id

                                downloadedRegions =
                                    downloadedRegions.filter {

                                        it.id != region.id
                                    }

                                regionSizes =
                                    regionSizes -
                                            region.id

                                scope.launch {

                                    offlineRepository
                                        .deleteRegion(
                                            region.name
                                        )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DistrictRowFromApi(
    region: RegionDto,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onOpen:()->Unit,
    onToggleDownload:()->Unit,
    onDelete:()->Unit,
    sizeMb:Float=0f
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            val imageUrl = remember(region.image) {
                buildImageUrl(region.image)
            }

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = region.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color(0xFFE7E3EB),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = region.name,
                    fontSize = 14.sp,
                    color = Color(0xFF1F1F1F)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = region.description ?: "Описание отсутствует",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF49454F)
                )

                if(isDownloaded){

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ){

                        Text(

                            text =
                                "✓ Доступно оффлайн • %.1f МБ"
                                    .format(sizeMb),

                            color =
                                Color(0xFF4CAF50),

                            fontSize = 11.sp
                        )

                        Spacer(
                            Modifier.width(8.dp)
                        )

                        Icon(

                            imageVector =
                                Icons.Outlined.Delete,

                            contentDescription =
                                "Удалить",

                            tint =
                                Color.Red,

                            modifier =
                                Modifier
                                    .size(16.dp)
                                    .clickable{

                                        onDelete()
                                    }
                        )
                    }
                }

            }

            if(isDownloading){

                Box(
                    contentAlignment=
                        Alignment.Center
                ){

                    CircularProgressIndicator(

                        progress = {
                            progress
                        },

                        modifier=
                            Modifier.size(
                                34.dp
                            )
                    )

                    Text(
                        "${(progress*100).toInt()}%",
                        fontSize=9.sp
                    )

                }

            }else{

        if(!isDownloaded){

            IconButton(
                onClick =
                    onToggleDownload
            ){

                Icon(

                    imageVector =
                        Icons.Outlined.Download,

                    contentDescription =
                        "Скачать"
                )
            }
        }
    }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFD9D9D9)
        )
    }
}

