package com.example.diplom.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class ImageDownloader(
    private val context: Context
) {

    private val client =
        OkHttpClient()

    suspend fun downloadImage(
        url: String,
        fileName: String
    ): String? = withContext(
        Dispatchers.IO
    ) {

        try {

            android.util.Log.d(
                "IMAGE_DL",
                "download=$url"
            )

            val request =
                Request.Builder()
                    .url(url)
                    .build()

            val response =
                client
                    .newCall(request)
                    .execute()

            if (!response.isSuccessful){

                android.util.Log.d(
                    "IMAGE_DL",
                    "error=${response.code}"
                )

                return@withContext null
            }

            val file =
                File(
                    context.filesDir,
                    fileName
                )

            response.body
                ?.byteStream()
                ?.use { input ->

                    file.outputStream()
                        .use { output ->

                            input.copyTo(
                                output
                            )
                        }
                }

            android.util.Log.d(
                "IMAGE_DL",
                "saved=${file.absolutePath}"
            )

            file.absolutePath

        } catch (e: Exception) {

            android.util.Log.e(
                "IMAGE_DL",
                "EXCEPTION",
                e
            )

            null
        }
    }
}