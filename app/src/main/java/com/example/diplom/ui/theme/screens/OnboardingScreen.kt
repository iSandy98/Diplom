package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.diplom.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.diplom.ui.theme.DiplomTheme
import androidx.compose.foundation.layout.systemBarsPadding

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val artRes: Int
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Открой Якутск\nзаново",
            subtitle = "Достопримечательности и\nинтересные места в одном\nприложении",
            artRes = R.drawable.onb_1
        ),
        OnboardingPage(
            title = "Выбирайте район\nили город",
            subtitle = "Находи места рядом и планируй\nпрогулки",
            artRes = R.drawable.onb_2
        ),
        OnboardingPage(
            title = "Начни с\nаудиогида",
            subtitle = "Слушай про достопримечательности\nне выходя из дома либо же на\nпрогулке",
            artRes = R.drawable.onb_3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding() // чтобы красиво с enableEdgeToEdge()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))

            //  Surface с shape (ничего не "вылезет")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFFF1F3F5)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    val page = pages[index]

                    // 1 слой = фон-картинка, 2 слой = контент
                    Box(modifier = Modifier.fillMaxSize()) {

                        // Фоновая картинка на всю карточку
                        Image(
                            painter = painterResource(id = page.artRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop // если фон должен покрывать весь блок
                            // если нужно без обрезания: поменяй на ContentScale.Fit
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 14.dp)
                        ) {
                            Text(
                                text = page.title,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B2B2B),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = page.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF4A4A4A),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.weight(1f))

                            DotsIndicator(
                                total = pages.size,
                                selected = pagerState.currentPage,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F2F2F),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.lastIndex) "Начать" else "Далее",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DotsIndicator(
    total: Int,
    selected: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (i == selected) Color(0xFF2F2F2F)
                        else Color(0xFFCFCFCF)
                    )
            )
        }
    }
}