package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.network.AuthRepository
import com.example.diplom.network.ProfileDto
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    isGuest: Boolean = false,
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()

    var profile by remember { mutableStateOf<ProfileDto?>(null) }
    var isLoading by remember { mutableStateOf(!isGuest) }

    LaunchedEffect(isGuest) {
        if (!isGuest) {
            scope.launch {
                val result = repository.getProfile()
                result.onSuccess {
                    profile = it
                }
                isLoading = false
            }
        }
    }

    val fullName = when {
        isGuest -> "Guest"
        profile != null -> listOfNotNull(profile?.first_name, profile?.last_name)
            .joinToString(" ")
            .ifBlank { profile?.username ?: "Пользователь" }
        else -> "Пользователь"
    }

    val isEn = com.example.diplom.utils.LanguageState.isEnglish

    val email = when {
        isGuest -> if (isEn) "Guest login" else "Гостевой вход"
        profile != null -> profile?.email ?: if (isEn) "Not specified" else "Не указан"
        else -> if (isEn) "Loading..." else "Загрузка..."
    }

    val phone = if (isEn) "Not specified" else "Не указан"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад"
                )
            }
            TextButton(
                onClick = {
                    com.example.diplom.utils.LanguageState.isEnglish =
                        !com.example.diplom.utils.LanguageState.isEnglish
                }
            ) {
                Text(
                    text = if (com.example.diplom.utils.LanguageState.isEnglish) "RU" else "EN",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFE7E3EB), CircleShape)
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = when {
                        isGuest && com.example.diplom.utils.LanguageState.isEnglish -> "Guest Profile"
                        isGuest -> "Гостевой профиль"
                        com.example.diplom.utils.LanguageState.isEnglish -> "Profile"
                        else -> "Профиль"
                    },
                    fontSize = 24.sp,
                    color = Color(0xFF1F1F1F),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val isEn = com.example.diplom.utils.LanguageState.isEnglish

                    ProfileItem(
                        icon = Icons.Outlined.Person,
                        label = if (isEn) "Full name" else "Полное имя",
                        value = fullName
                    )

                    Spacer(Modifier.height(16.dp))

                    ProfileItem(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = email
                    )

                    Spacer(Modifier.height(16.dp))

                    ProfileItem(
                        icon = Icons.Outlined.Phone,
                        label = if (isEn) "Phone number" else "Номер телефона",
                        value = phone
                    )

                    Spacer(Modifier.height(28.dp))

                    if (!isGuest) {
                        Button(
                            onClick = onEditProfile,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isEn) "Edit Profile" else "Редактировать профиль")
                        }

                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F1F1F),
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isEn) "Log out" else "Выйти")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1F1F1F)
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF1F1F1F)
            )
        }
    }
}