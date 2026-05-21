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

private const val API_DOMAIN = "http://10.0.2.2:8000"

@Composable
fun DistrictsScreen(
    onBack: () -> Unit,
    onOpenDistrict: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { RegionsRepository(context) }
    val pointsRepository = remember {
        PointsRepository(context)
    }
    val offlineRepository = remember {
        OfflineRepository(context)
    }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var regions by remember { mutableStateOf<List<RegionDto>>(emptyList()) }
    var downloadedIds by remember {

        mutableStateOf(
            setOf<Int>()
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
        val result = repository.getRegions()
        result.onSuccess {
            regions = it
        }.onFailure {
            errorText = it.message ?: "Ошибка загрузки районов"
        }
        isLoading = false
    }

    val filteredRegions = remember(query, regions) {
        if (query.isBlank()) {
            regions
        } else {
            regions.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    LaunchedEffect(
        downloadingRegionId
    ){

        if(
            downloadingRegionId==null
        ) return@LaunchedEffect

        for(
        i in 1..100
        ){

            kotlinx.coroutines.delay(
                60
            )

            downloadProgress =
                i/100f
        }

        downloadedIds =
            downloadedIds +
                    downloadingRegionId!!

        downloadingRegionId =
            null
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

                                    val pointsResult =
                                        pointsRepository
                                            .getPoints()

                                    val districtPlaces =
                                        pointsResult
                                            .getOrDefault(
                                                emptyList()
                                            )
                                            .filter {

                                                it.region_name ==
                                                        region.name
                                            }

                                    offlineRepository
                                        .saveRegionPlaces(

                                            region.name,

                                            districtPlaces
                                        )
                                }
                            },

                            onDelete = {

                                downloadedIds =
                                    downloadedIds -
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
    onDelete:()->Unit
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
                                "✓ Доступно оффлайн • ~34 МБ",

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

private fun buildImageUrl(imagePath: String?): String? {
    if (imagePath.isNullOrBlank()) return null

    return when {
        imagePath.startsWith("http://") || imagePath.startsWith("https://") -> imagePath
        imagePath.startsWith("/") -> "$API_DOMAIN$imagePath"
        else -> "$API_DOMAIN/$imagePath"
    }
}