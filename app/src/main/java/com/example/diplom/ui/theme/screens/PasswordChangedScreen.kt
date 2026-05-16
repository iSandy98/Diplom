package com.example.diplom.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diplom.R
import kotlinx.coroutines.delay

@Composable
fun PasswordChangedScreen(
    onGoToLogin: () -> Unit
) {
    var showHeader by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        showHeader = true
        delay(180)
        showButton = true
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn(animationSpec = tween(280)) +
                        slideInVertically(
                            animationSpec = tween(280),
                            initialOffsetY = { it / 6 }
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.registration_art),
                        contentDescription = null,
                        modifier = Modifier.size(width = 185.dp, height = 158.dp)
                    )

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = "Пароль успешно изменён",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF2B2B2B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Теперь вы можете войти с новым паролем.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6A6A6A),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(animationSpec = tween(260)) +
                        slideInVertically(
                            animationSpec = tween(260),
                            initialOffsetY = { it / 8 }
                        )
            ) {
                Button(
                    onClick = onGoToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F2F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text("Войти", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}