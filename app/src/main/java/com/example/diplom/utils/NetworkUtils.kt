package com.example.diplom.utils

const val API_DOMAIN = "http://10.0.2.2:8000"

fun buildImageUrl(imagePath: String?): String? {
    if (imagePath.isNullOrBlank()) return null

    return when {
        imagePath.startsWith("http://") || imagePath.startsWith("https://") -> imagePath
        imagePath.startsWith("/") -> "$API_DOMAIN$imagePath"
        else -> "$API_DOMAIN/$imagePath"
    }
}