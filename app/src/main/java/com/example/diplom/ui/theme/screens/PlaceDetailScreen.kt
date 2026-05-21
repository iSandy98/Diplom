package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.example.diplom.network.PointDetailDto
import com.example.diplom.network.PointsRepository
import kotlinx.coroutines.delay
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import com.example.diplom.network.AuthRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private const val API_DOMAIN = "http://10.0.2.2:8000"

@Composable
fun PlaceDetailScreen(
    placeId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { PointsRepository(context) }
    val authRepository = remember {
        AuthRepository(context)
    }

    val canReview =
        !authRepository.isGuest()

    var selectedStars by remember {
        mutableIntStateOf(0)
    }

    var reviewText by remember {
        mutableStateOf("")
    }

    var place by remember { mutableStateOf<PointDetailDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var showPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }

    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val playerDuration = exoPlayer.duration
                if (playerDuration > 0) {
                    duration = playerDuration
                }

                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    currentPosition = 0L

                    exoPlayer.stop()       //  ВАЖНО
                    exoPlayer.clearMediaItems()

                    showPlayer = false
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playerError = error.message ?: "Ошибка воспроизведения аудио"
                isPlaying = false
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying, showPlayer) {
        while (isPlaying && showPlayer) {
            currentPosition = exoPlayer.currentPosition
            val playerDuration = exoPlayer.duration
            if (playerDuration > 0) {
                duration = playerDuration
            }
            delay(500)
        }
    }

    LaunchedEffect(placeId) {
        isLoading = true
        errorText = null

        val id = placeId.toIntOrNull()
        if (id == null) {
            errorText = "Некорректный идентификатор места"
            isLoading = false
            return@LaunchedEffect
        }

        val result = repository.getPointDetail(id)
        result.onSuccess {
            place = it
        }.onFailure {
            errorText = it.message ?: "Ошибка загрузки достопримечательности"
        }

        isLoading = false
    }

    val photos = place?.photos.orEmpty()
    val pagerState = rememberPagerState(
        pageCount = { if (photos.isEmpty()) 1 else photos.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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

            place != null -> {
                val currentPlace = place!!
                val shortInfo = buildString {
                    append(currentPlace.region?.name ?: "Район не указан")
                    currentPlace.category?.name?.let {
                        append(" • ")
                        append(it)
                    }
                }

                Column(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                        .padding(bottom = if (showPlayer) 140.dp else 0.dp)
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
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }

                        Text(
                            text = "Страница",
                            fontSize = 20.sp,
                            color = Color(0xFF1F1F1F)
                        )

                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = shortInfo,
                            color = Color(0xFF49454F),
                            fontSize = 14.sp
                        )

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        if(currentPlace.audio_guide==null){

                            Text(

                                text =
                                    "🎧 Аудиогид скоро появится",

                                color =
                                    Color.Gray,

                                fontSize = 13.sp
                            )
                        }

                        if (currentPlace.audio_guide != null) {

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val audioUrl =
                                        buildMediaUrl(
                                            currentPlace.audio_guide?.audio_file
                                        )

                                    playerError = null

                                    if (!audioUrl.isNullOrBlank()) {

                                        exoPlayer.stop()

                                        exoPlayer.clearMediaItems()

                                        exoPlayer.setMediaItem(
                                            MediaItem.fromUri(audioUrl)
                                        )

                                        exoPlayer.prepare()

                                        exoPlayer.playWhenReady = true

                                        currentPosition = 0L
                                        duration = 0L
                                        showPlayer = true

                                    } else {

                                        playerError =
                                            "Ссылка на аудио не найдена"
                                    }
                                },

                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color(0xFFE49A68)
                                    ),

                                shape =
                                    RoundedCornerShape(24.dp)

                            ) {

                                Text("Послушать аудиогид")

                                Spacer(
                                    Modifier.width(8.dp)
                                )

                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = formatDate(currentPlace.created_at),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = currentPlace.description ?: "Описание отсутствует",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFF1F1F1F),
                        lineHeight = 22.sp
                    )


                    Spacer(
                        Modifier.height(24.dp)
                    )

                    if (!playerError.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = playerError ?: "",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) { page ->
                        if (photos.isNotEmpty()) {
                            AsyncImage(
                                model = buildImageUrl(photos[page].image),
                                contentDescription = photos[page].caption,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxSize()
                                    .background(
                                        Color(0xFFE7E3EB),
                                        RoundedCornerShape(20.dp)
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(if (photos.isEmpty()) 1 else photos.size) { index ->
                            val color =
                                if (pagerState.currentPage == index) Color.Black
                                else Color.LightGray

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .background(color, RoundedCornerShape(50))
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(24.dp)
                    )

                    Text(
                        text = "Отзывы и рейтинг",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp
                            )
                    )

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Text(
                        text = "Оцените место",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp
                            )
                    )

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    if (canReview) {

                        Row(

                            modifier =
                                Modifier.padding(
                                    horizontal = 16.dp
                                )

                        ) {

                            repeat(5) { index ->

                                IconButton(

                                    onClick = {

                                        selectedStars =
                                            index + 1
                                    }

                                ) {

                                    Icon(

                                        imageVector =

                                            if (
                                                index <
                                                selectedStars
                                            )

                                                Icons.Default.Star

                                            else

                                                Icons.Outlined.StarBorder,

                                        contentDescription = null,

                                        tint =
                                            Color(
                                                0xFFFFC107
                                            )
                                    )
                                }
                            }
                            if(selectedStars>0){

                                Spacer(
                                    Modifier.height(6.dp)
                                )

                                Text(

                                    text =
                                        "Ваша оценка: " +
                                                "$selectedStars/5",

                                    modifier =
                                        Modifier.padding(
                                            horizontal=16.dp
                                        ),

                                    color=
                                        Color.Gray
                                )
                            }
                        }

                        OutlinedTextField(

                            value =
                                reviewText,

                            onValueChange = {

                                reviewText = it
                            },

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp
                                    ),

                            placeholder = {

                                Text(
                                    "Напишите отзыв..."
                                )
                            }
                        )

                        Spacer(
                            Modifier.height(10.dp)
                        )

                        Button(

                            onClick = {

                                // API позже
                            },

                            modifier =
                                Modifier.padding(
                                    horizontal = 16.dp
                                )

                        ) {

                            Text(
                                "Отправить"
                            )
                        }

                    } else {

                        Card(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp
                                    ),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFFF6F4F8)
                                ),

                            shape =
                                RoundedCornerShape(
                                    20.dp
                                )
                        ) {

                            Column(

                                modifier =
                                    Modifier.padding(
                                        16.dp
                                    )
                            ) {

                                Text(
                                    "Только зарегистрированные пользователи могут оставлять отзывы"
                                )

                                Spacer(
                                    Modifier.height(
                                        8.dp
                                    )
                                )

                                Button(

                                    onClick = {

                                        // позже переход на экран входа
                                    },

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                Color(0xFF5264A5)
                                        ),

                                    shape =
                                        RoundedCornerShape(
                                            18.dp
                                        )

                                ) {

                                    Text(
                                        "Войти"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(
                        Modifier.height(24.dp)
                    )

                    Spacer(Modifier.height(24.dp))
                }

                if (showPlayer && currentPlace.audio_guide != null) {
                    val audio = currentPlace.audio_guide!!
                    val safeDuration = if (duration > 0) duration else 1L
                    val progress = (currentPosition.coerceAtMost(safeDuration)).toFloat() / safeDuration.toFloat()
                    val remaining = (duration - currentPosition).coerceAtLeast(0L)

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = Color(0xFFF4D7C3),
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (photos.isNotEmpty()) {
                                    AsyncImage(
                                        model = buildImageUrl(photos.first().image),
                                        contentDescription = currentPlace.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                Color(0xFFE7E3EB),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = currentPlace.name,
                                        color = Color(0xFF1F1F1F)
                                    )

                                    Text(
                                        text = buildString {
                                            append(audio.language ?: "Язык не указан")
                                            audio.duration_seconds?.let {
                                                append(" • ")
                                                append("${it} сек")
                                            }
                                        },
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.playWhenReady = true
                                            exoPlayer.play()
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        exoPlayer.seekTo(0)
                                        exoPlayer.playWhenReady = true
                                        exoPlayer.play()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.SkipNext,
                                        contentDescription = null
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        exoPlayer.pause()
                                        exoPlayer.seekTo(0)
                                        isPlaying = false
                                        currentPosition = 0L
                                        showPlayer = false
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Закрыть плеер"
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Slider(
                                value = progress,
                                onValueChange = { newValue ->
                                    val newPosition = (safeDuration * newValue).toLong()
                                    currentPosition = newPosition
                                },
                                onValueChangeFinished = {
                                    exoPlayer.seekTo(currentPosition)
                                },
                                valueRange = 0f..1f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatDuration(currentPosition),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )

                                Text(
                                    text = "${formatDuration(duration)} • осталось ${formatDuration(remaining)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildImageUrl(imagePath: String?): String? {
    if (imagePath.isNullOrBlank()) return null

    return when {
        imagePath.contains("127.0.0.1") ->
            imagePath.replace("127.0.0.1", "10.0.2.2")
        imagePath.startsWith("http://") || imagePath.startsWith("https://") -> imagePath
        imagePath.startsWith("/") -> "$API_DOMAIN$imagePath"
        else -> "$API_DOMAIN/$imagePath"
    }
}

private fun buildMediaUrl(audioPath: String?): String? {
    if (audioPath.isNullOrBlank()) return null

    return when {
        audioPath.contains("127.0.0.1") ->
            audioPath.replace("127.0.0.1", "10.0.2.2")
        audioPath.startsWith("http") -> audioPath
        audioPath.startsWith("/") -> "$API_DOMAIN$audioPath"
        else -> "$API_DOMAIN/$audioPath"
    }
}

private fun formatDate(date: String?): String {
    if (date.isNullOrBlank()) return ""

    return try {
        val parsed = OffsetDateTime.parse(date)
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        parsed.format(formatter)
    } catch (_: Exception) {
        date
    }
}

private fun formatDuration(milliseconds: Long): String {
    if (milliseconds <= 0L) return "00:00"

    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d", minutes, seconds)
}