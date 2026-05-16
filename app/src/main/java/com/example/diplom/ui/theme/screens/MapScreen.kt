package com.example.diplom.ui.theme.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.diplom.network.PointsRepository
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

data class MapPlaceUi(
    val id: String,
    val title: String,
    val point: Point,
    val regionName: String? = null,
    val hasAudio: Boolean = false
)

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onOpenPlace: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { PointsRepository(context) }

    var places by remember { mutableStateOf<List<MapPlaceUi>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<MapPlaceUi?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val mapView = remember {
        MapView(context).apply {
            mapWindow.map.move(
                CameraPosition(
                    Point(62.0281, 129.7326),
                    13.8f,
                    0.0f,
                    0.0f
                )
            )
            mapWindow.map.isNightModeEnabled = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            userLocation = getLastKnownLocation(context)
            if (userLocation == null) {
                locationError = "Не удалось определить местоположение"
            } else {
                locationError = null
                val point = Point(userLocation!!.latitude, userLocation!!.longitude)
                mapView.mapWindow.map.move(
                    CameraPosition(point, 15.5f, 0.0f, 0.0f)
                )
            }
        } else {
            locationError = "Разрешение на геолокацию не выдано"
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()

        onDispose {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    LaunchedEffect(Unit) {
        val result = repository.getPoints()

        result.onSuccess { list ->
            places = list.mapNotNull { dto ->
                val lat = dto.latitude
                val lon = dto.longitude

                if (lat != null && lon != null) {
                    MapPlaceUi(
                        id = dto.id.toString(),
                        title = dto.name,
                        point = Point(lat, lon),
                        regionName = dto.region_name,
                        hasAudio = dto.has_audio == true
                    )
                } else {
                    null
                }
            }

            if (places.isNotEmpty()) {
                mapView.mapWindow.map.move(
                    CameraPosition(
                        places.first().point,
                        13.8f,
                        0.0f,
                        0.0f
                    )
                )
            }
        }.onFailure {
            errorText = it.message ?: "Ошибка загрузки точек"
        }

        isLoading = false
    }

    val markerBitmap = remember { createMarkerBitmap() }
    val userMarkerBitmap = remember { createUserMarkerBitmap() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(Modifier.height(16.dp))

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
            text = "Карта",
            fontSize = 28.sp,
            color = Color(0xFF1F1F1F),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFD9D9D9),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Поиск места, города",
                color = Color(0xFF666666),
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Поиск",
                tint = Color(0xFF444444)
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.weight(1f)
        ) {
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
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { mapView },
                        update = { view ->
                            val map = view.mapWindow.map
                            map.isNightModeEnabled = true
                            map.mapObjects.clear()

                            val markerImage = ImageProvider.fromBitmap(markerBitmap)

                            places.forEach { place ->
                                val placemark = map.mapObjects.addPlacemark().apply {
                                    geometry = place.point
                                    setIcon(
                                        markerImage,
                                        IconStyle().apply {
                                            anchor = PointF(0.5f, 1.0f)
                                            scale = 1.0f
                                        }
                                    )
                                }

                                val tapListener = MapObjectTapListener { _, _ ->
                                    selectedPlace = place
                                    map.move(
                                        CameraPosition(
                                            place.point,
                                            15.5f,
                                            0.0f,
                                            0.0f
                                        )
                                    )
                                    true
                                }

                                placemark.addTapListener(tapListener)
                            }

                            userLocation?.let { location ->
                                val userPoint = Point(location.latitude, location.longitude)
                                val userMarker = ImageProvider.fromBitmap(userMarkerBitmap)

                                map.mapObjects.addPlacemark().apply {
                                    geometry = userPoint
                                    setIcon(
                                        userMarker,
                                        IconStyle().apply {
                                            anchor = PointF(0.5f, 0.5f)
                                            scale = 1.0f
                                        }
                                    )
                                }
                            }
                        }
                    )

                    FloatingActionButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (granted) {
                                userLocation = getLastKnownLocation(context)
                                if (userLocation != null) {
                                    locationError = null
                                    val point = Point(
                                        userLocation!!.latitude,
                                        userLocation!!.longitude
                                    )
                                    mapView.mapWindow.map.move(
                                        CameraPosition(point, 15.5f, 0.0f, 0.0f)
                                    )
                                } else {
                                    locationError = "Не удалось определить местоположение"
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Моё местоположение"
                        )
                    }

                    selectedPlace?.let { place ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 88.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = place.title,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1F1F1F)
                                )

                                place.regionName?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = if (place.hasAudio) "$it • Аудиогид" else it,
                                        fontSize = 13.sp,
                                        color = Color(0xFF666666)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedPlace = null },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Закрыть")
                                    }

                                    Button(
                                        onClick = { onOpenPlace(place.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFD382),
                                            contentColor = Color(0xFF1F1F1F)
                                        )
                                    ) {
                                        Text("Перейти")
                                    }
                                }
                            }
                        }
                    }

                    locationError?.let {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(12.dp),
                                color = Color.Red
                            )
                        }
                    }

                    if (places.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет точек для отображения",
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getLastKnownLocation(context: Context): Location? {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted && !coarseGranted) return null

    val gpsLocation = runCatching {
        manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }.getOrNull()

    val networkLocation = runCatching {
        manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }.getOrNull()

    return when {
        gpsLocation != null && networkLocation != null ->
            if (gpsLocation.time >= networkLocation.time) gpsLocation else networkLocation
        gpsLocation != null -> gpsLocation
        else -> networkLocation
    }
}

private fun createMarkerBitmap(): Bitmap {
    val width = 96
    val height = 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
    }

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#D86B4D")
    }

    canvas.drawCircle(width / 2f, height / 2f, 42f, outerPaint)
    canvas.drawCircle(width / 2f, height / 2f, 34f, innerPaint)

    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#F5D07A")
    }

    canvas.drawRoundRect(
        RectF(28f, 26f, 68f, 66f),
        10f,
        10f,
        accentPaint
    )

    return bitmap
}

private fun createUserMarkerBitmap(): Bitmap {
    val width = 52
    val height = 52
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
    }

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#3B82F6")
    }

    canvas.drawCircle(width / 2f, height / 2f, 24f, outerPaint)
    canvas.drawCircle(width / 2f, height / 2f, 16f, innerPaint)

    return bitmap
}