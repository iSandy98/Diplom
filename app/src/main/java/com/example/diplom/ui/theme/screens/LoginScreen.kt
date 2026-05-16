package com.example.diplom.ui.theme.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R
import com.example.diplom.network.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (email: String, password: String, remember: Boolean) -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var remember by rememberSaveable { mutableStateOf(true) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository(context) }

    val borderColor = Color(0xFFD9D9D9)
    val cardShape = RoundedCornerShape(8.dp)

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color(0xFF2B2B2B)
                    )
                }
                Text(
                    text = "Вход",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2B2B2B)
                )
            }

            Spacer(Modifier.height(70.dp))

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

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp)
                        .wrapContentHeight()
                        .border(1.dp, borderColor, cardShape)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2B2B2B)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Введите email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = borderColor,
                            focusedBorderColor = borderColor,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Пароль",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2B2B2B)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Введите пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = borderColor,
                            focusedBorderColor = borderColor,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = remember,
                            onCheckedChange = { remember = it },
                            enabled = !isLoading,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2F2F2F),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Запомнить пароль",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2B2B2B)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Заполните email и пароль",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            scope.launch {
                                isLoading = true

                                val result = repository.login(
                                    email = email.trim(),
                                    password = password
                                )

                                result.onSuccess {
                                    Toast.makeText(
                                        context,
                                        "Вход выполнен",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onLogin(email.trim(), password, remember)
                                }.onFailure { error ->
                                    error.printStackTrace()

                                    val message = if (error is retrofit2.HttpException) {
                                        try {
                                            error.response()?.errorBody()?.string() ?: "Ошибка входа"
                                        } catch (_: Exception) {
                                            error.message ?: "Ошибка входа"
                                        }
                                    } else {
                                        error.message ?: "Ошибка входа"
                                    }

                                    Toast.makeText(
                                        context,
                                        message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F2F2F),
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Вход", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Забыли пароль?",
                        color = Color(0xFF2B2B2B),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(enabled = !isLoading, onClick = onForgotPassword)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "By continuing I agree with Privacy Policy\nand Terms & Conditions",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFFB7B7B7),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )
        }
    }
}