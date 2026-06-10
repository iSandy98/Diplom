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
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.driving.DrivingRouterType
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import com.example.diplom.network.PointDetailDto
import androidx.compose.material.icons.filled.Close
import com.example.diplom.network.OfflineRepository
import com.example.diplom.network.RouteDetailDto
import kotlinx.coroutines.launch
import android.location.LocationListener
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.diplom.network.RoutePointDto

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
    val offlineRepository = remember { OfflineRepository(context) }
    val drivingRouter = remember {
        DirectionsFactory.getInstance().createDrivingRouter(
            DrivingRouterType.COMBINED
        )
    }

    var drivingSession by remember {
        mutableStateOf<DrivingSession?>(null)
    }
    var places by remember { mutableStateOf<List<MapPlaceUi>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<MapPlaceUi?>(null) }
    var searchText by remember {
        mutableStateOf("")
    }

    var userBearing by remember {
        mutableFloatStateOf(0f)
    }

    var routePlaces by remember {
        mutableStateOf<List<MapPlaceUi>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var routeBuilt by remember {
        mutableStateOf(false)
    }
    var routeDistance by remember {
        mutableStateOf("")
    }

    var routeTime by remember {
        mutableStateOf("")
    }
    var routePolyline by remember {
        mutableStateOf<com.yandex.mapkit.map.PolylineMapObject?>(null)
    }
    var tourStarted by remember {
        mutableStateOf(false)
    }

    var currentPointIndex by remember {
        mutableStateOf(0)
    }

    var nearbyPlace by remember {
        mutableStateOf<MapPlaceUi?>(null)
    }

    var audioPlace by remember {
        mutableStateOf<MapPlaceUi?>(null)
    }

    var audioShown by remember {
        mutableStateOf(setOf<String>())
    }

    var alreadyTriggered by remember {
        mutableStateOf(setOf<String>())
    }

    var nearbyDistance by remember {
        mutableFloatStateOf(0f)
    }

    var userPlacemark by remember {
        mutableStateOf<com.yandex.mapkit.map.PlacemarkMapObject?>(null)
    }

    var exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    var currentAudioPlace by remember {
        mutableStateOf<PointDetailDto?>(null)
    }

    val locationManager = remember {

        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager
    }

    val listener = remember {

        LocationListener { location ->

            userLocation = location
        }
    }

    var audioStarted by remember {
        mutableStateOf(setOf<String>())
    }


    var routes by remember {
        mutableStateOf<List<RouteDetailDto>>(emptyList())
    }

    var showRoutesDialog by remember {
        mutableStateOf(false)
    }

    val placemarks = remember {
        mutableStateListOf<com.yandex.mapkit.map.PlacemarkMapObject>()
    }

    val scope =
        rememberCoroutineScope()


    val filteredPlaces = remember(
        places,
        searchText
    ) {

        if (searchText.isBlank()) {

            places

        } else {

            places.filter {

                it.title.contains(
                    searchText,
                    ignoreCase = true
                )
            }
        }
    }

    val sensorManager = remember {

        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    }

    val rotationSensor = remember {

        sensorManager.getDefaultSensor(
            Sensor.TYPE_ROTATION_VECTOR
        )
    }

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

    DisposableEffect(Unit) {

        onDispose {

            try {

                locationManager.removeUpdates(
                    listener
                )

            } catch(_:Exception){}
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

        DisposableEffect(rotationSensor) {

            val listener =

                object : SensorEventListener {

                    override fun onSensorChanged(
                        event: SensorEvent
                    ) {

                        val rotationMatrix =
                            FloatArray(9)

                        SensorManager.getRotationMatrixFromVector(

                            rotationMatrix,

                            event.values
                        )

                        val orientation =
                            FloatArray(3)

                        SensorManager.getOrientation(

                            rotationMatrix,

                            orientation
                        )

                        userBearing =

                            Math.toDegrees(

                                orientation[0].toDouble()

                            ).toFloat()
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int
                    ) {}
                }

            rotationSensor?.let {

                sensorManager.registerListener(

                    listener,

                    it,

                    SensorManager.SENSOR_DELAY_UI
                )
            }

            onDispose {

                sensorManager.unregisterListener(
                    listener
                )
            }
    }

    DisposableEffect(exoPlayer) {

        val listener = object : androidx.media3.common.Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {

                if (state == androidx.media3.common.Player.STATE_ENDED) {

                    if (currentPointIndex < routePlaces.lastIndex) {

                        currentPointIndex++

                        routePlaces.getOrNull(currentPointIndex)
                            ?.let { nextPoint ->

                                mapView.mapWindow.map.move(
                                    CameraPosition(
                                        nextPoint.point,
                                        15f,
                                        0f,
                                        0f
                                    )
                                )
                            }

                    } else {

                        tourStarted = false
                        currentAudioPlace = null
                    }
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {

            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(    Unit) {

        val granted =

            ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED


        if(granted){

            try {

                locationManager.requestLocationUpdates(

                    LocationManager.GPS_PROVIDER,

                    1000,

                    1f,

                    listener

                )

            } catch(_:SecurityException){}
        }

        userLocation = getLastKnownLocation(context)


        val routesResult =
            repository.getRoutes()

        routesResult.onSuccess {

            routes = it

        }.onFailure {

            val offlineRoutes =
                offlineRepository.getRoutes()

            routes = offlineRoutes.map { route ->

                val points =

                    offlineRepository
                        .getRoutePoints(
                            route.id
                        )

                RouteDetailDto(

                    id =
                        route.id,

                    name =
                        route.name,

                    description =
                        route.description,

                    duration_minutes =
                        route.duration,

                    route_points =

                        points.map {

                            RoutePointDto(

                                order =
                                    it.order,

                                point_id =
                                    it.pointId,

                                name =
                                    it.name ?: "Без названия",

                                latitude =
                                    it.latitude.toString(),

                                longitude =
                                    it.longitude.toString(),

                                note = ""
                            )
                        },

                    points_count =
                        points.size,

                    avg_rating = 0.0,

                    difficulty_display =
                        "Лёгкий"
                )
            }
        }

        val result = repository.getPoints()

        result.onFailure {

            android.util.Log.e(
                "POINT_ERROR",
                "ошибка=${it.message}",
                it
            )
        }
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

                val offlinePlaces =
                    offlineRepository
                        .getAllPlaces()

            android.util.Log.d(
                "OFFLINE_MAP",
                "найдено=${offlinePlaces.size}"
            )

            offlinePlaces.forEach {
                android.util.Log.d(
                    "OFFLINE_MAP",
                    "${it.name} | ${it.regionName}"
                )
            }

                places =
                    offlinePlaces.mapNotNull {

                        if(
                            it.latitude != null &&
                            it.longitude != null
                        ){

                            MapPlaceUi(

                                id =
                                    it.id.toString(),

                                title =
                                    it.name,

                                point =
                                    Point(
                                        it.latitude,
                                        it.longitude
                                    ),

                                regionName =
                                    it.regionName,

                                hasAudio =
                                    it.hasAudio
                            )

                        } else null
                    }

                if (places.isEmpty()) {

                    errorText =
                        "Нет оффлайн данных"

                } else {

                    errorText = null
                }
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
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = {
                    com.example.diplom.utils.LanguageState.isEnglish =
                        !com.example.diplom.utils.LanguageState.isEnglish
                }
            ) {
                Text(
                    text = if (com.example.diplom.utils.LanguageState.isEnglish) "RU" else "EN",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (com.example.diplom.utils.LanguageState.isEnglish) "Map" else "Карта",
            fontSize = 28.sp,
            color = Color(0xFF1F1F1F),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(

            value = searchText,

            onValueChange = {

                searchText = it

            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),

            placeholder = {

                Text(
                    if (com.example.diplom.utils.LanguageState.isEnglish) "Search place, city" else "Поиск места, города"
                )

            },

            trailingIcon = {

                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null
                )

            },

            shape =
                RoundedCornerShape(12.dp),

            singleLine = true
        )

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

                        factory = {
                            mapView
                        },

                        update = { view ->

                            view.mapWindow.map
                                .isNightModeEnabled = true
                        }
                    )
                    currentAudioPlace?.let{

                        Surface(

                            modifier =
                                Modifier
                                    .align(
                                        Alignment.BottomCenter
                                    )
                                    .fillMaxWidth(),

                            tonalElevation=8.dp

                        ){

                            Row(

                                modifier=
                                    Modifier
                                        .padding(14.dp),

                                verticalAlignment=
                                    Alignment.CenterVertically

                            ){

                                Column(
                                    Modifier.weight(1f)
                                ){

                                    Text(
                                        if (com.example.diplom.utils.LanguageState.isEnglish) "🎧 Now playing" else "🎧 Сейчас играет"
                                    )

                                    Text(
                                        it.name
                                    )
                                }

                                IconButton(

                                    onClick={

                                        exoPlayer.pause()

                                        currentAudioPlace =
                                            null
                                    }

                                ){

                                    Icon(
                                        Icons.Default.Close,
                                        null
                                    )
                                }
                            }
                        }
                    }

                    if(
                        tourStarted &&
                        routePlaces.isNotEmpty()
                    ){

                        Card(

                            modifier =
                                Modifier
                                    .align(
                                        Alignment.TopCenter
                                    )
                                    .padding(
                                        top = 16.dp
                                    ),

                            shape =
                                RoundedCornerShape(
                                    20.dp
                                )

                        ){

                            Row(

                                modifier =
                                    Modifier.padding(
                                        14.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically

                            ){

                                Column{

                                    Text(
                                        if (com.example.diplom.utils.LanguageState.isEnglish) "🎧 Tour in progress" else "🎧 Экскурсия идет"
                                    )

                                    Text(

                                        text = if (com.example.diplom.utils.LanguageState.isEnglish)
                                            "Point ${currentPointIndex+1} of ${routePlaces.size}"
                                        else
                                            "Точка ${currentPointIndex+1} из ${routePlaces.size}",

                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(
                                    Modifier.width(
                                        16.dp
                                    )
                                )

                                OutlinedButton(

                                    onClick = {

                                        tourStarted = false
                                        currentPointIndex = 0

                                        routePlaces = emptyList()
                                        routeBuilt = false
                                        selectedPlace = null

                                        nearbyPlace = null
                                        alreadyTriggered = emptySet()

                                        routeDistance = ""
                                        routeTime = ""

                                        routePolyline?.let { polyline ->

                                            if (polyline.isValid) {
                                                mapView
                                                    .mapWindow
                                                    .map
                                                    .mapObjects
                                                    .remove(polyline)
                                            }
                                        }

                                        routePolyline = null
                                    }

                                ){

                                    Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Stop" else "Стоп")
                                }
                            }
                        }
                    }

                    nearbyPlace?.let { place ->

                        LaunchedEffect(place.id) {

                            kotlinx.coroutines.delay(3500)

                            nearbyPlace = null
                        }

                        Card(

                            modifier =
                                Modifier
                                    .align(
                                        Alignment.BottomCenter
                                    )
                                    .padding(
                                        bottom = 110.dp
                                    )
                                    .padding(
                                        top = 140.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    )
                                    .zIndex(999f),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFF4CAF50)
                                )
                        ) {

                            Column(

                                modifier =
                                    Modifier.padding(16.dp)

                            ) {

                                Text(

                                    text = if (com.example.diplom.utils.LanguageState.isEnglish) "📍 You are near an attraction" else "📍 Вы рядом с объектом",

                                    color =
                                        Color.White
                                )

                                Text(

                                    text =
                                        "${nearbyDistance.toInt()} м",

                                    fontSize = 12.sp,

                                    color =
                                        Color.White.copy(alpha = 0.75f)
                                )

                                Spacer(
                                    Modifier.height(6.dp)
                                )

                                Text(

                                    text =
                                        place.title,

                                    color =
                                        Color.White
                                )
                            }
                        }
                    }

                    audioPlace?.let { place ->

                        Card(

                            modifier =
                                Modifier
                                    .align(
                                        Alignment.BottomCenter
                                    )
                                    .padding(
                                        bottom = 110.dp
                                    )
                                    .padding(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFF1F1F1F)
                                )
                        ){

                            Column(

                                modifier =
                                    Modifier.padding(16.dp),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ){

                                Text(
                                    if (com.example.diplom.utils.LanguageState.isEnglish) "🎧 Audio guide ready" else "🎧 Аудиогид готов",
                                    color = Color.White
                                )

                                Spacer(
                                    Modifier.height(6.dp)
                                )

                                Text(
                                    place.title,
                                    color = Color.White
                                )

                                Spacer(
                                    Modifier.height(10.dp)
                                )

                                Row(

                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)

                                ){

                                    OutlinedButton(

                                        onClick = {

                                            audioPlace = null
                                        }

                                    ){

                                        Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Later" else "Позже")
                                    }

                                    Button(

                                        onClick = {

                                            onOpenPlace(
                                                place.id
                                            )

                                            audioPlace = null
                                        }

                                    ){

                                        Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Listen" else "Слушать")
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(
                        filteredPlaces,
                        userLocation,
                        routePolyline,
                        routePlaces
                    ) {

                        val map =
                            mapView.mapWindow.map

                        if (!map.mapObjects.isValid)
                            return@LaunchedEffect

                        placemarks.forEach { placemark ->

                            try {

                                if (placemark.isValid) {

                                    map.mapObjects.remove(
                                        placemark
                                    )
                                }

                            } catch (_: Exception) {

                            }
                        }

                        placemarks.clear()

                        // удаляем только маршрут
                        routePolyline?.let {

                            if (!it.isValid)
                                routePolyline = null
                        }

                        val markerImage =
                            ImageProvider.fromBitmap(
                                markerBitmap
                            )

                        // не clear()

                        filteredPlaces.forEachIndexed { index, place ->

                            val placemark =
                                map.mapObjects.addPlacemark()

                            placemark.zIndex = 100f

                            placemarks.add(placemark)

                            placemark.geometry =
                                place.point

                            val routeIndex =
                                routePlaces.indexOfFirst {
                                    it.id == place.id
                                }

                            val icon =

                                if (routeIndex != -1) {

                                    ImageProvider.fromBitmap(
                                        createRouteNumberBitmap(
                                            routeIndex + 1
                                        )
                                    )

                                } else {

                                    markerImage
                                }

                            placemark.setIcon(

                                icon,

                                IconStyle().apply {

                                    anchor =
                                        PointF(0.5f,1f)

                                    scale = 1f
                                }
                            )

                            placemark.addTapListener(
                                MapObjectTapListener { _, _ ->

                                    if (!tourStarted) {

                                        selectedPlace = place

                                        map.move(
                                            CameraPosition(
                                                place.point,
                                                15.5f,
                                                0f,
                                                0f
                                            )
                                        )
                                    }

                                    true
                                }
                            )
                        }

                        userLocation?.let { location ->

                            userPlacemark?.let {

                                if (it.isValid) {

                                    map.mapObjects.remove(it)
                                }
                            }

                            val userMarker =
                                ImageProvider.fromBitmap(
                                    userMarkerBitmap
                                )

                            userPlacemark =
                                map.mapObjects.addPlacemark().apply {

                                    direction =
                                        userBearing

                                    zIndex = 200f

                                    geometry =
                                        Point(
                                            location.latitude,
                                            location.longitude
                                        )

                                    setIcon(
                                        userMarker,
                                        IconStyle().apply {

                                            anchor =
                                                PointF(0.5f,0.5f)

                                            scale = 1f

                                            rotationType =

                                                com.yandex.mapkit.map.RotationType.ROTATE
                                        }
                                    )
                                }
                        }
                    }

                    LaunchedEffect(
                        userLocation,
                        currentPointIndex,
                        tourStarted
                    ) {

                        if (
                            !tourStarted ||
                            userLocation == null ||
                            routePlaces.isEmpty()
                        ) return@LaunchedEffect

                        val currentPlace =
                            routePlaces.getOrNull(currentPointIndex)
                                ?: return@LaunchedEffect

                        val result = FloatArray(1)

                        android.location.Location.distanceBetween(
                            userLocation!!.latitude,
                            userLocation!!.longitude,
                            currentPlace.point.latitude,
                            currentPlace.point.longitude,
                            result
                        )

                        val distance = result[0]

                        val alreadyNear =
                            alreadyTriggered.contains(currentPlace.id)

                        if (distance < 50f && !alreadyNear) {

                            nearbyPlace = currentPlace
                            nearbyDistance = distance

                            alreadyTriggered =
                                alreadyTriggered + currentPlace.id
                        }

                        if (
                            distance < 20f &&
                            currentPlace.hasAudio &&
                            !audioStarted.contains(currentPlace.id)
                        ) {

                            android.util.Log.d(
                                "AUDIO_TEST",
                                "условие выполнено"
                            )

                            val detailResult =
                                repository.getPointDetail(
                                    currentPlace.id.toInt()
                                )

                            detailResult.onSuccess { detail ->

                                android.util.Log.d(
                                    "AUDIO_TEST",
                                    "детали пришли"
                                )

                                android.util.Log.d(
                                    "AUDIO_TEST",
                                    "audio=${detail.audio_guide?.audio_file}"
                                )

                                val audioUrl =
                                    buildMediaUrl(
                                        detail.audio_guide?.audio_file
                                    )

                                android.util.Log.d(
                                    "AUDIO_TEST",
                                    "url=$audioUrl"
                                )

                                if (!audioUrl.isNullOrBlank()) {

                                    audioPlace =
                                        currentPlace

                                    exoPlayer.stop()

                                    exoPlayer.clearMediaItems()

                                    exoPlayer.setMediaItem(
                                        MediaItem.fromUri(audioUrl)
                                    )

                                    exoPlayer.prepare()

                                    exoPlayer.play()

                                    currentAudioPlace =
                                        detail

                                    audioStarted =
                                        audioStarted + currentPlace.id
                                }
                            }

                            detailResult.onFailure {
                                val offlineAudio = offlineRepository.getAudio(currentPlace.id.toInt())
                                val audioUrl = if (offlineAudio?.audioUrl?.startsWith("/data/") == true)
                                    "file://${offlineAudio.audioUrl}"
                                else
                                    buildMediaUrl(offlineAudio?.audioUrl)
                                if (!audioUrl.isNullOrBlank()) {
                                    audioPlace = currentPlace
                                    exoPlayer.stop()
                                    exoPlayer.clearMediaItems()
                                    exoPlayer.setMediaItem(MediaItem.fromUri(audioUrl))
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                    currentAudioPlace = PointDetailDto(
                                        id = currentPlace.id.toInt(),
                                        name = currentPlace.title,
                                        description = null,
                                        latitude = currentPlace.point.latitude,
                                        longitude = currentPlace.point.longitude,
                                        category = null,
                                        region = null,
                                        photos = null,
                                        audio_guide = null,
                                        created_at = null,
                                        updated_at = null
                                    )
                                    audioStarted = audioStarted + currentPlace.id
                                }
                            }
                        }
                    }

                    LaunchedEffect(userLocation, tourStarted) {

                        if (tourStarted || userLocation == null || places.isEmpty())
                            return@LaunchedEffect

                        val nearest = places
                            .filter { it.hasAudio }
                            .minByOrNull { place ->
                                val r = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    userLocation!!.latitude,
                                    userLocation!!.longitude,
                                    place.point.latitude,
                                    place.point.longitude,
                                    r
                                )
                                r[0]
                            } ?: return@LaunchedEffect

                        val r = FloatArray(1)
                        android.location.Location.distanceBetween(
                            userLocation!!.latitude,
                            userLocation!!.longitude,
                            nearest.point.latitude,
                            nearest.point.longitude,
                            r
                        )
                        val dist = r[0]

                        if (dist < 50f && !alreadyTriggered.contains(nearest.id)) {
                            nearbyPlace = nearest
                            nearbyDistance = dist
                            alreadyTriggered = alreadyTriggered + nearest.id
                        }

                        if (dist < 30f && !audioStarted.contains(nearest.id)) {
                            val detailResult =
                                repository.getPointDetail(nearest.id.toInt())
                            detailResult.onSuccess { detail ->
                                val audioUrl =
                                    buildMediaUrl(detail.audio_guide?.audio_file)
                                if (!audioUrl.isNullOrBlank()) {
                                    audioPlace = nearest
                                    exoPlayer.stop()
                                    exoPlayer.clearMediaItems()
                                    exoPlayer.setMediaItem(MediaItem.fromUri(audioUrl))
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                    currentAudioPlace = detail
                                    audioStarted = audioStarted + nearest.id
                                }
                            }
                            detailResult.onFailure {
                                val offlineAudio = offlineRepository.getAudio(nearest.id.toInt())
                                val audioUrl = if (offlineAudio?.audioUrl?.startsWith("/data/") == true)
                                    "file://${offlineAudio.audioUrl}"
                                else
                                    buildMediaUrl(offlineAudio?.audioUrl)
                                if (!audioUrl.isNullOrBlank()) {
                                    audioPlace = nearest
                                    exoPlayer.stop()
                                    exoPlayer.clearMediaItems()
                                    exoPlayer.setMediaItem(MediaItem.fromUri(audioUrl))
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                    currentAudioPlace = PointDetailDto(
                                        id = nearest.id.toInt(),
                                        name = nearest.title,
                                        description = null,
                                        latitude = nearest.point.latitude,
                                        longitude = nearest.point.longitude,
                                        category = null,
                                        region = null,
                                        photos = null,
                                        audio_guide = null,
                                        created_at = null,
                                        updated_at = null
                                    )
                                    audioStarted = audioStarted + nearest.id
                                }
                            }
                        }
                    }

                    if(
                        routePlaces.isNotEmpty()
                        &&
                        !tourStarted
                    ) {

                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                Text(

                                    text = if (com.example.diplom.utils.LanguageState.isEnglish) {
                                        if (routeBuilt) "Route built" else "Route: ${routePlaces.size} object(s)"
                                    } else {
                                        if (routeBuilt) "Маршрут построен" else "Маршрут: ${routePlaces.size} объект(ов)"
                                    }

                                )
                                Spacer(
                                    Modifier.height(6.dp)
                                )

                                Text(
                                    text =
                                        "${routePlaces.size} объекта • " +
                                                routeDistance + " • " +
                                                routeTime,

                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )

                                Spacer(
                                    Modifier.height(8.dp)
                                )

                                if (!routeBuilt) {

                                    Button(

                                        onClick = {
                                            userLocation?.let { location ->
                                                buildMultiRoute(
                                                    mapView = mapView,
                                                    router = drivingRouter,
                                                    userLocation = location,
                                                    places = routePlaces,
                                                    onInfoLoaded = { distance, time ->
                                                        routeDistance = distance
                                                        routeTime = time
                                                    },
                                                    routePolyline = routePolyline,
                                                    onPolylineChanged = { routePolyline = it }
                                                )
                                                routeBuilt = true
                                            }
                                        }

                                    ) {

                                        Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Build" else "Построить")
                                    }
                                }
                                if (routeBuilt && !tourStarted) {

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    Button(

                                        onClick = {

                                            tourStarted = true
                                            currentPointIndex = 0

                                        }

                                    ) {

                                        Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Start Tour" else "Начать экскурсию")
                                    }
                                }

                                if (
                                    tourStarted &&
                                    routePlaces.isNotEmpty()
                                ) {

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    Text(

                                        text = if (com.example.diplom.utils.LanguageState.isEnglish) "Next point:" else "Следующая точка:",

                                        fontSize = 13.sp,

                                        color = Color.Gray
                                    )

                                    routePlaces.getOrNull(
                                        currentPointIndex
                                    )?.let {

                                        Text(
                                            text = it.title,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(
                                        Modifier.height(8.dp)
                                    )

                                    if (
                                        currentPointIndex <
                                        routePlaces.lastIndex
                                    ) {

                                        Button(

                                            onClick = {

                                                if(
                                                    currentPointIndex <
                                                    routePlaces.lastIndex
                                                ){

                                                    currentPointIndex++

                                                    routePlaces
                                                        .getOrNull(
                                                            currentPointIndex
                                                        )
                                                        ?.let { nextPoint ->

                                                            mapView
                                                                .mapWindow
                                                                .map
                                                                .move(

                                                                    CameraPosition(
                                                                        nextPoint.point,
                                                                        15f,
                                                                        0f,
                                                                        0f
                                                                    )
                                                                )
                                                        }
                                                }
                                            }

                                        ) {

                                            Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Next" else "Следующая")
                                        }

                                    } else {

                                        Text(

                                            text = if (com.example.diplom.utils.LanguageState.isEnglish) "Tour completed 🎉" else "Экскурсия завершена 🎉",

                                            color =
                                                Color(0xFF4CAF50)
                                        )
                                    }
                                }

                                TextButton(

                                    onClick = {

                                        routePlaces = emptyList()

                                        selectedPlace = null

                                        routeBuilt = false

                                        tourStarted = false
                                        currentPointIndex = 0

                                        routeDistance = ""
                                        routeTime = ""

                                        routePolyline?.let { polyline ->

                                            if (polyline.isValid) {

                                                mapView
                                                    .mapWindow
                                                    .map
                                                    .mapObjects
                                                    .remove(polyline)
                                            }
                                        }

                                        routePolyline = null

                                        // заставляем карту обновить объекты
                                        mapView.mapWindow.map.move(
                                            mapView.mapWindow.map.cameraPosition
                                        )
                                    }

                                ) {

                                    Text(if (com.example.diplom.utils.LanguageState.isEnglish) "End Route" else "Завершить маршрут")

                                }

                            }

                        }

                    }

                    ExtendedFloatingActionButton(

                        onClick = {

                            showRoutesDialog = true
                        },

                        modifier =
                            Modifier
                                .align(
                                    Alignment.BottomStart
                                )
                                .padding(16.dp),

                        containerColor =
                            Color(0xFF4CAF50)

                    ) {

                        Text(
                            if (com.example.diplom.utils.LanguageState.isEnglish) "🗺 Routes" else "🗺 Маршруты"
                        )
                    }


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
                                        text = if (place.hasAudio) "$it • ${if (com.example.diplom.utils.LanguageState.isEnglish) "Audio guide" else "Аудиогид"}" else it,
                                        fontSize = 13.sp,
                                        color = Color(0xFF666666)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    Button(
                                        onClick = {

                                            if (
                                                routePlaces.none {
                                                    it.id == place.id
                                                }
                                            ) {

                                                routePlaces =
                                                    routePlaces + place

                                                selectedPlace = null
                                            }

                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Add to route" else "Добавить в маршрут")
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {

                                        OutlinedButton(
                                            onClick = {
                                                selectedPlace = null
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Close" else "Закрыть")
                                        }

                                        Button(
                                            onClick = {
                                                onOpenPlace(place.id)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor =
                                                        Color(0xFFFFD382),
                                                    contentColor =
                                                        Color(0xFF1F1F1F)
                                                )
                                        ) {
                                            Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Go to" else "Перейти")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(showRoutesDialog){

                        AlertDialog(

                            onDismissRequest = {

                                showRoutesDialog=false
                            },

                            title = {

                                Text(
                                    if (com.example.diplom.utils.LanguageState.isEnglish) "Ready Routes" else "Готовые маршруты"
                                )
                            },

                            text = {

                                Column{

                                    routes.forEach { route ->

                                        Card(

                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical=6.dp),

                                            onClick = {

                                                scope.launch {

                                                    if (!isOnline(context)) {
                                                        val fallback = routes.find { r -> r.id == route.id }
                                                        val offlinePlaces = fallback?.route_points?.mapNotNull { rp ->
                                                            val lat = rp.latitude?.toDoubleOrNull()
                                                            val lon = rp.longitude?.toDoubleOrNull()
                                                            if (lat != null && lon != null) {
                                                                MapPlaceUi(
                                                                    id = rp.point_id.toString(),
                                                                    title = rp.name,
                                                                    point = Point(lat, lon),
                                                                    hasAudio = true
                                                                )
                                                            } else null
                                                        } ?: emptyList()

                                                        if (offlinePlaces.isNotEmpty()) {
                                                            val sorted = userLocation?.let { loc ->
                                                                offlinePlaces.sortedBy { p ->
                                                                    val r = FloatArray(1)
                                                                    android.location.Location.distanceBetween(
                                                                        loc.latitude, loc.longitude,
                                                                        p.point.latitude, p.point.longitude, r
                                                                    )
                                                                    r[0]
                                                                }
                                                            } ?: offlinePlaces
                                                            routePlaces = sorted
                                                            routeBuilt = true
                                                        }
                                                        showRoutesDialog = false
                                                        selectedPlace = null
                                                        return@launch
                                                    }

                                                    val detailResult =

                                                        repository.getRouteDetail(
                                                            route.id
                                                        )

                                                    detailResult.onSuccess { detail ->

                                                        val loadedPlaces =

                                                            detail.route_points.mapNotNull {

                                                                val lat =
                                                                    it.latitude
                                                                        ?.toDoubleOrNull()

                                                                val lon =
                                                                    it.longitude
                                                                        ?.toDoubleOrNull()

                                                                if(
                                                                    lat != null &&
                                                                    lon != null
                                                                ){

                                                                    MapPlaceUi(

                                                                        id =
                                                                            it.point_id.toString(),

                                                                        title =
                                                                            it.name,

                                                                        point =
                                                                            Point(
                                                                                lat,
                                                                                lon
                                                                            ),

                                                                        hasAudio = true
                                                                    )

                                                                } else null
                                                            }

                                                        routePlaces =

                                                            userLocation?.let { location ->

                                                                loadedPlaces.sortedBy {

                                                                    val result =
                                                                        FloatArray(1)

                                                                    android.location.Location.distanceBetween(

                                                                        location.latitude,
                                                                        location.longitude,

                                                                        it.point.latitude,
                                                                        it.point.longitude,

                                                                        result
                                                                    )

                                                                    result[0]
                                                                }

                                                            } ?: loadedPlaces

                                                        showRoutesDialog =
                                                            false

                                                        routeBuilt =
                                                            false

                                                        selectedPlace =
                                                            null

                                                        userLocation?.let { location ->
                                                            buildMultiRoute(
                                                                mapView = mapView,
                                                                router = drivingRouter,
                                                                userLocation = location,
                                                                places = routePlaces,
                                                                onInfoLoaded = { distance, time ->
                                                                    routeDistance = distance
                                                                    routeTime = time
                                                                },
                                                                routePolyline = routePolyline,
                                                                onPolylineChanged = { routePolyline = it }
                                                            )
                                                            routeBuilt = true
                                                        }
                                                    }.onFailure {

                                                        // Offline fallback: use route_points from already-loaded routes list
                                                        val fallback = routes.find { r -> r.id == route.id }
                                                        val offlinePlaces = fallback?.route_points?.mapNotNull { rp ->
                                                            val lat = rp.latitude?.toDoubleOrNull()
                                                            val lon = rp.longitude?.toDoubleOrNull()
                                                            if (lat != null && lon != null) {
                                                                MapPlaceUi(
                                                                    id = rp.point_id.toString(),
                                                                    title = rp.name,
                                                                    point = Point(lat, lon),
                                                                    hasAudio = true
                                                                )
                                                            } else null
                                                        } ?: emptyList()

                                                        if (offlinePlaces.isNotEmpty()) {
                                                            val sorted = userLocation?.let { loc ->
                                                                offlinePlaces.sortedBy { p ->
                                                                    val r = FloatArray(1)
                                                                    android.location.Location.distanceBetween(
                                                                        loc.latitude, loc.longitude,
                                                                        p.point.latitude, p.point.longitude, r
                                                                    )
                                                                    r[0]
                                                                }
                                                            } ?: offlinePlaces
                                                            routePlaces = sorted
                                                            routeBuilt = true
                                                        }

                                                        showRoutesDialog = false
                                                        selectedPlace = null
                                                    }
                                                }
                                            }
                                        ) {

                                            Column(

                                                Modifier.padding(
                                                    12.dp
                                                )
                                            ){

                                                Text(
                                                    route.name
                                                )

                                                Spacer(
                                                    Modifier.height(4.dp)
                                                )

                                                Text(

                                                    if (com.example.diplom.utils.LanguageState.isEnglish)
                                                        "${route.points_count} points • ${route.duration_minutes} min"
                                                    else
                                                        "${route.points_count} точек • ${route.duration_minutes} мин",

                                                    color=Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            },

                            confirmButton = {

                                TextButton(
                                    onClick = {

                                        showRoutesDialog=false
                                    }
                                ){

                                    Text(if (com.example.diplom.utils.LanguageState.isEnglish) "Close" else "Закрыть")
                                }
                            }
                        )
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
                                text = if (com.example.diplom.utils.LanguageState.isEnglish) "No points to display" else "Нет точек для отображения",
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}
private fun getLastKnownLocation(
    context: Context
): Location? {

    val manager =
        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

    val fineGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted && !coarseGranted)
        return null

    try {

        val providers =
            manager.getProviders(true)

        for (provider in providers) {

            val location =
                manager.getLastKnownLocation(provider)

            if (location != null)
                return location
        }

    } catch (_: Exception) { }

    // координаты Якутска для эмулятора
    return Location("mock").apply {
        latitude = 62.026373
        longitude = 129.735630
    }
}

private fun buildRoute(
    mapView: MapView,
    router: DrivingRouter,
    userLocation: Location,
    destination: Point
) {

    val startPoint = Point(
        userLocation.latitude,
        userLocation.longitude
    )

    val points = listOf(

        RequestPoint(
            startPoint,
            RequestPointType.WAYPOINT,
            null,
            null,
            null
        ),

        RequestPoint(
            destination,
            RequestPointType.WAYPOINT,
            null,
            null,
            null
        )
    )

    router.requestRoutes(
        points,
        DrivingOptions(),
        VehicleOptions(),

        object : DrivingSession.DrivingRouteListener {

            override fun onDrivingRoutes(
                routes: MutableList<DrivingRoute>
            ) {

                if (routes.isNotEmpty()) {

                    mapView.mapWindow.map.mapObjects
                        .addPolyline(routes[0].geometry)
                }
                val points = routes[0].geometry.points

                if (points.isNotEmpty()) {

                    var minLat = points.first().latitude
                    var maxLat = points.first().latitude

                    var minLon = points.first().longitude
                    var maxLon = points.first().longitude

                    points.forEach {

                        minLat = minOf(
                            minLat,
                            it.latitude
                        )

                        maxLat = maxOf(
                            maxLat,
                            it.latitude
                        )

                        minLon = minOf(
                            minLon,
                            it.longitude
                        )

                        maxLon = maxOf(
                            maxLon,
                            it.longitude
                        )
                    }

                    val center = Point(

                        (minLat + maxLat) / 2,
                        (minLon + maxLon) / 2

                    )

                    mapView.mapWindow.map.move(

                        CameraPosition(
                            center,
                            11f,
                            0f,
                            0f
                        )
                    )
                }
            }

            override fun onDrivingRoutesError(
                error: com.yandex.runtime.Error
            ) {

            }
        }
    )
}

private fun buildMultiRoute(
    mapView: MapView,
    router: DrivingRouter,
    userLocation: Location,
    places: List<MapPlaceUi>,
    onInfoLoaded: (
        String,
        String
    ) -> Unit,
    routePolyline: com.yandex.mapkit.map.PolylineMapObject?,
    onPolylineChanged: (com.yandex.mapkit.map.PolylineMapObject?) -> Unit
) {

    val startPoint = Point(
        userLocation.latitude,
        userLocation.longitude
    )

    val points =
        mutableListOf<RequestPoint>()

    points.add(
        RequestPoint(
            startPoint,
            RequestPointType.WAYPOINT,
            null,
            null,
            null
        )
    )

    places.forEachIndexed { index, place ->

        points.add(

            RequestPoint(

                place.point,

                if(
                    index == places.lastIndex
                )

                    RequestPointType.WAYPOINT

                else

                    RequestPointType.VIAPOINT,

                null,
                null,
                null
            )
        )
    }


    router.requestRoutes(
        points,
        DrivingOptions(),
        VehicleOptions(),
        object : DrivingSession.DrivingRouteListener {

            override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                if (routes.isEmpty()) {
                    drawStraightPolyline(
                        mapView, startPoint, places,
                        routePolyline, onInfoLoaded, onPolylineChanged
                    )
                    return
                }
                val route = routes[0]
                val distanceKm = route.metadata.weight.distance.value / 1000
                val timeMinutes = route.metadata.weight.time.value / 60

                onInfoLoaded(
                    String.format("%.1f км", distanceKm),
                    "~${timeMinutes.toInt()} мин"
                )

                val mapObjects = mapView.mapWindow.map.mapObjects
                routePolyline?.let { mapObjects.remove(it) }

                val polyline = mapObjects.addPolyline(routes[0].geometry)
                @Suppress("DEPRECATION")
                polyline.strokeWidth = 3f
                polyline.outlineWidth = 1f
                polyline.zIndex = -1f

                onPolylineChanged(polyline)
            }

            override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {
                drawStraightPolyline(
                    mapView, startPoint, places,
                    routePolyline, onInfoLoaded, onPolylineChanged
                )
            }
        }
    )
}

private fun drawStraightPolyline(
    mapView: MapView,
    startPoint: Point?,
    places: List<MapPlaceUi>,
    currentPolyline: com.yandex.mapkit.map.PolylineMapObject?,
    onInfoLoaded: (String, String) -> Unit,
    onPolylineChanged: (com.yandex.mapkit.map.PolylineMapObject?) -> Unit
) {
    val allPoints = buildList {
        startPoint?.let { add(it) }
        addAll(places.map { it.point })
    }
    if (allPoints.size < 2) return
    val mapObjects = mapView.mapWindow.map.mapObjects

    currentPolyline?.let {
        if (it.isValid) mapObjects.remove(it)
    }

    val polyline = mapObjects.addPolyline(Polyline(allPoints))
    @Suppress("DEPRECATION")
    polyline.strokeWidth = 5f
    polyline.outlineWidth = 1f
    polyline.zIndex = 10f
    polyline.setStrokeColor(android.graphics.Color.argb(255, 41, 121, 255))

    android.util.Log.d(
        "POLYLINE",
        "pts=${allPoints.size} valid=${polyline.isValid} " +
        allPoints.joinToString { "(${it.latitude},${it.longitude})" }
    )

    onInfoLoaded("${places.size} точек", "Пешком")
    onPolylineChanged(polyline)
}

private fun isOnline(context: android.content.Context): Boolean {
    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
            as android.net.ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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
    val arrowPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                AndroidColor.WHITE
        }

    val path =
        android.graphics.Path()

    path.moveTo(
        width/2f,
        6f
    )

    path.lineTo(
        width/2f-8,
        22f
    )

    path.lineTo(
        width/2f+8,
        22f
    )

    path.close()

    canvas.drawPath(
        path,
        arrowPaint
    )

    return bitmap
}

private fun createRouteNumberBitmap(
    number: Int
): Bitmap {

    val size = 100

    val bitmap =
        Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

    val canvas =
        Canvas(bitmap)

    val circlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                AndroidColor.parseColor("#4CAF50")
        }

    canvas.drawCircle(
        size/2f,
        size/2f,
        42f,
        circlePaint
    )

    val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                AndroidColor.WHITE

            textSize = 42f

            textAlign =
                Paint.Align.CENTER

            isFakeBoldText = true
        }

    canvas.drawText(
        number.toString(),
        size/2f,
        size/2f + 14,
        textPaint
    )

    return bitmap
}

private fun buildMediaUrl(
    path: String?
): String? {

    if (path.isNullOrBlank())
        return null

    return when {
        path.startsWith("http") ->
            path

        path.startsWith("/") ->
            "https://yave4en.pythonanywhere.com$path"

        else ->
            "https://yave4en.pythonanywhere.com/$path"
    }
}