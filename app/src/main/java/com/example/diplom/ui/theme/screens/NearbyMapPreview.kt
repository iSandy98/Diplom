package com.example.diplom.ui.theme.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
fun NearbyMapPreview(
    places: List<PlaceUi>,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val mapPlaces = remember(places) {

        places.mapNotNull {

            val lat =
                it.latitude

            val lon =
                it.longitude

            if(
                lat!=null &&
                lon!=null
            ){

                MapPlaceUi(

                    id =
                        it.id,

                    title =
                        it.title,

                    point =
                        Point(
                            lat,
                            lon
                        ),

                    regionName =
                        "",

                    hasAudio =
                        false
                )

            }else null
        }
    }

    var isLoading by remember {

        mutableStateOf(false)
    }

    val mapView = remember {

        MapView(context).apply {

            mapWindow.map.move(
                CameraPosition(
                    Point(
                        62.0281,
                        129.7326
                    ),
                    13.8f,
                    0f,
                    0f
                )
            )

            mapWindow.map
                .isNightModeEnabled = true
        }
    }

    LaunchedEffect(Unit){
        isLoading=false
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()

        onDispose {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }

    val markerBitmap = remember { createPreviewMarkerBitmap() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (mapPlaces.isEmpty()) {
            Text("Нет точек для отображения")
        } else {
            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp),
                update = { view ->
                    val map = view.mapWindow.map
                    map.isNightModeEnabled = true
                    map.mapObjects.clear()

                    val markerImage = ImageProvider.fromBitmap(markerBitmap)

                    mapPlaces.forEach { place ->
                        map.mapObjects.addPlacemark().apply {
                            geometry = place.point
                            setIcon(
                                markerImage,
                                IconStyle().apply {
                                    anchor = PointF(0.5f, 1.0f)
                                    scale = 0.8f
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}

private fun createPreviewMarkerBitmap(): Bitmap {
    val width = 84
    val height = 84
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
    }

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#D86B4D")
    }

    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.parseColor("#F5D07A")
    }

    canvas.drawCircle(width / 2f, height / 2f, 36f, outerPaint)
    canvas.drawCircle(width / 2f, height / 2f, 29f, innerPaint)

    canvas.drawRoundRect(
        RectF(26f, 24f, 58f, 56f),
        8f,
        8f,
        accentPaint
    )

    return bitmap
}