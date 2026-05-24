package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplom.network.StoriesRepository
import com.example.diplom.network.StoryDto
import com.example.diplom.utils.buildImageUrl
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.diplom.database.DatabaseProvider

@Composable
fun StoryDetailScreen(
    storyId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StoriesRepository(context) }

    val database =
        remember {
            DatabaseProvider
                .getDatabase(context)
        }

    var story by remember { mutableStateOf<StoryDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(storyId) {
        isLoading = true
        errorText = null

        val id = storyId.toIntOrNull()
        if (id == null) {
            errorText = "Некорректный идентификатор истории"
            isLoading = false
            return@LaunchedEffect
        }

        val result =
            repository.getStoryDetail(id)

        result.onSuccess {

            story = it

        }.onFailure {

            val offlineStory =

                database
                    .offlineStoryDao()
                    .getStory(id)

            if(offlineStory!=null){

                story = StoryDto(

                    id =
                        offlineStory.id,

                    title =
                        offlineStory.title,

                    description =
                        offlineStory.description,

                    image =
                        offlineStory.image,

                    created_at =
                        offlineStory.createdAt
                )

            }else{

                errorText =
                    "История недоступна оффлайн"
            }
        }

        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorText != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorText ?: "Ошибка",
                    color = Color.Red
                )
            }
        }

        story != null -> {
            val currentStory = story!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color(0xFF1F1F1F)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = currentStory.title,
                    style = TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 36.sp
                    ),
                    color = Color(0xFF1F1F1F),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = formatStoryDate(currentStory.created_at),
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(16.dp))

                if (!currentStory.image.isNullOrBlank()) {
                    AsyncImage(
                        model = buildImageUrl(currentStory.image),
                        contentDescription = currentStory.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = currentStory.description ?: "Описание отсутствует",
                    color = Color(0xFF1F1F1F),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun formatStoryDate(date: String?): String {
    if (date.isNullOrBlank()) return ""

    return try {
        val parsed = OffsetDateTime.parse(date)
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        parsed.format(formatter)
    } catch (_: Exception) {
        date
    }
}