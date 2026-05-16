package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R

@Composable
fun OtpVerificationScreen(
    email: String,
    onBack: () -> Unit,
    onContinue: (code: String) -> Unit
) {
    // Храним каждую цифру отдельно — так проще делать фокус/удаление
    var d0 by rememberSaveable { mutableStateOf("") }
    var d1 by rememberSaveable { mutableStateOf("") }
    var d2 by rememberSaveable { mutableStateOf("") }
    var d3 by rememberSaveable { mutableStateOf("") }

    val code = d0 + d1 + d2 + d3

    val r0 = remember { FocusRequester() }
    val r1 = remember { FocusRequester() }
    val r2 = remember { FocusRequester() }
    val r3 = remember { FocusRequester() }

    // Чтобы сразу можно было вводить
    LaunchedEffect(Unit) { r0.requestFocus() }

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
                    text = "Код верификации",
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

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Мы отправили код на вашу электронную почту $email",
                fontSize = 16.sp,
                color = Color(0xFF2B2B2B)
            )

            Spacer(Modifier.height(28.dp))

            //  Центрируем ряд квадратиков
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OtpBox(
                        value = d0,
                        placeholder = "0",
                        focusRequester = r0,
                        onValueChange = { v ->
                            d0 = v
                            if (v.isNotEmpty()) r1.requestFocus()
                        },
                        onBackspaceWhenEmpty = { /* нет предыдущего */ }
                    )

                    OtpBox(
                        value = d1,
                        placeholder = "0",
                        focusRequester = r1,
                        onValueChange = { v ->
                            d1 = v
                            if (v.isNotEmpty()) r2.requestFocus()
                        },
                        onBackspaceWhenEmpty = { r0.requestFocus() }
                    )

                    OtpBox(
                        value = d2,
                        placeholder = "0",
                        focusRequester = r2,
                        onValueChange = { v ->
                            d2 = v
                            if (v.isNotEmpty()) r3.requestFocus()
                        },
                        onBackspaceWhenEmpty = { r1.requestFocus() }
                    )

                    OtpBox(
                        value = d3,
                        placeholder = "0",
                        focusRequester = r3,
                        onValueChange = { v ->
                            d3 = v
                            // последний — фокус не переводим дальше
                        },
                        onBackspaceWhenEmpty = { r2.requestFocus() }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onContinue(code) },
                enabled = code.length == 4,
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

@Composable
private fun OtpBox(
    value: String,
    placeholder: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspaceWhenEmpty: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .size(64.dp)
            .background(Color(0xFFF2F2F2), shape),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { new ->
                //  только 1 цифра
                val filtered = new.filter { it.isDigit() }.take(1)
                onValueChange(filtered)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = 24.sp,
                color = Color(0xFF2B2B2B),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                //  backspace-назад, если пусто
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                        if (value.isEmpty()) {
                            onBackspaceWhenEmpty()
                            true
                        } else {
                            // если не пусто — просто очистим
                            onValueChange("")
                            true
                        }
                    } else false
                },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    //  серый "0" как в макете
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 24.sp,
                            color = Color(0xFFBDBDBD),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}