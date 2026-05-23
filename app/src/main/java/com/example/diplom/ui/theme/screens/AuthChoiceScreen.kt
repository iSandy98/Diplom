package com.example.diplom.ui.theme.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diplom.R
import com.example.diplom.network.AuthRepository
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun AuthChoiceScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onGuestSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository(context) }

    var isGuestLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.registr_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 42.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthButton(
                text = "Регистрация",
                onClick = onRegister
            )

            Spacer(modifier = Modifier.height(15.dp))

            AuthButton(
                text = "Вход",
                onClick = onLogin
            )

            Spacer(modifier = Modifier.height(15.dp))

            AuthButton(
                text = if (isGuestLoading) "Подключение..." else "Гостевой режим",
                enabled = !isGuestLoading,
                onClick = {

                    scope.launch {

                        isGuestLoading = true

                        try {

                            val result =
                                repository
                                    .loginAsGuest()

                            result.onSuccess {

                                Toast.makeText(

                                    context,

                                    "Гостевой вход выполнен",

                                    Toast.LENGTH_SHORT

                                ).show()

                                onGuestSuccess()

                            }.onFailure {

                                throw Exception()
                            }

                        } catch (
                            e: Exception
                        ) {

                            if(
                                repository
                                    .isLoggedIn()
                            ){

                                Toast.makeText(

                                    context,

                                    "Открыт оффлайн режим",

                                    Toast.LENGTH_SHORT

                                ).show()

                                onGuestSuccess()

                            } else {

                                Toast.makeText(

                                    context,

                                    "Для первого входа нужен интернет",

                                    Toast.LENGTH_LONG

                                ).show()
                            }
                        }

                        isGuestLoading = false
                    }
                }
            )

            if (isGuestLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2F2F2F),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF5A5A5A),
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}