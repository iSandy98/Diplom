package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onContinue: (email: String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }

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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color(0xFF2B2B2B)
                    )
                }
                Text(
                    text = "Восстановление пароля",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2B2B2B)
                )
            }

            Spacer(Modifier.height(70.dp))

            // Иллюстрация 185x158
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.registration_art),
                    contentDescription = null,
                    modifier = Modifier.size(width = 185.dp, height = 158.dp)
                )
            }

            Spacer(Modifier.height(40.dp))

            // Описание 16sp
            Text(
                text = "Пожалуйста, введите свой адрес электронной почты, и мы вышлем вам одноразовый пароль для сброса пароля.",
                style = TextStyle(
                    fontSize = 16.sp
                ),
                color = Color(0xFF2B2B2B)
            )

            Spacer(Modifier.height(20.dp))

            // Поле Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        "example@gmail.com",
                        fontSize = 16.sp
                    )
                },
                label = {
                    Text(
                        "Email",
                        fontSize = 12.sp
                    )
                },
                textStyle = TextStyle(
                    fontSize = 16.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onContinue(email.trim()) },
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
                    "Продолжить",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}