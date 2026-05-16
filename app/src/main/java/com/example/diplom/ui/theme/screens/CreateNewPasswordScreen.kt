package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R

private enum class PasswordStrength { WEAK, MEDIUM, STRONG }

private fun passwordStrength(p: String): PasswordStrength {
    var score = 0
    if (p.length >= 8) score++
    if (p.any { it.isDigit() }) score++
    if (p.any { it.isUpperCase() }) score++
    if (p.any { !it.isLetterOrDigit() }) score++
    return when {
        score >= 3 -> PasswordStrength.STRONG
        score == 2 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.WEAK
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewPasswordScreen(
    onBack: () -> Unit,
    onContinue: (password: String) -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val borderColor = Color(0xFFD9D9D9)
    val cardShape = RoundedCornerShape(8.dp)

    val passwordsMatch = password.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            password == confirmPassword

    // ✅ сила пароля (пересчитывается при изменении password)
    val strength = remember(password) { passwordStrength(password) }

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
                    text = "Создание нового пароля",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2B2B2B)
                )
            }

            Spacer(Modifier.height(70.dp))

            // Иллюстрация
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

            // Карточка
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp)
                        .border(1.dp, borderColor, cardShape)
                        .padding(24.dp)
                ) {

                    Text(
                        text = "Придумайте новый пароль",
                        fontSize = 16.sp,
                        color = Color(0xFF2B2B2B)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Новый пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = borderColor,
                            focusedBorderColor = borderColor,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    // ✅ Индикатор силы пароля
                    Spacer(Modifier.height(10.dp))

                    val strengthText = when (strength) {
                        PasswordStrength.WEAK -> "Слабый пароль"
                        PasswordStrength.MEDIUM -> "Средний пароль"
                        PasswordStrength.STRONG -> "Надёжный пароль"
                    }

                    val strengthColor = when (strength) {
                        PasswordStrength.WEAK -> Color(0xFFB00020)
                        PasswordStrength.MEDIUM -> Color(0xFF8A6D00)
                        PasswordStrength.STRONG -> Color(0xFF1B5E20)
                    }

                    Text(
                        text = strengthText,
                        fontSize = 12.sp,
                        color = strengthColor
                    )

                    Spacer(Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = when (strength) {
                            PasswordStrength.WEAK -> 0.33f
                            PasswordStrength.MEDIUM -> 0.66f
                            PasswordStrength.STRONG -> 1f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        trackColor = Color(0xFFEDEDED),
                        color = strengthColor
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Повторите новый пароль",
                        fontSize = 16.sp,
                        color = Color(0xFF2B2B2B)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Повторите пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = borderColor,
                            focusedBorderColor = borderColor,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Пароли не совпадают",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onContinue(password) },
                enabled = passwordsMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F2F2F),
                    contentColor = Color.White
                )
            ) {
                Text("Продолжить")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}