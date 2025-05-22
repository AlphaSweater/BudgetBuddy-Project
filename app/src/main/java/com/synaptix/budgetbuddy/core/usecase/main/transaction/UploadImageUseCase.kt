package com.synaptix.budgetbuddy.core.usecase.main.transaction

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import org.json.JSONObject
import javax.inject.Inject

//imgur client id
private const val IMGUR_CLIENT_ID = "c3363df746ecd23"


class UploadImageUseCase @Inject constructor() {
    sealed class UploadImageResult {
        data class Success(val imageUrl: String) : UploadImageResult()
        data class Error(val message: String) : UploadImageResult()
    }

    private val client = OkHttpClient()


//    fun execute(imageBytes: ByteArray): UploadImageResult {
//        return try{
//            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
//
//            //builds request body for imgur
//            val requestBody = FormBody.Builder()
//                .add("image", base64Image)
//                .add("type", "base64")
//                .build()
//
//            //builds Http request for imgur
//            val request = Request.Builder()
//                .url("https://api.imgur.com/3/image")
//                .addHeader("Authorization", "Client-ID ${IMGUR_CLIENT_ID.trim()}")
//                .post(requestBody)
//                .build()
//
//            // Execute the request
//            val response = client.newCall(request).execute()
//            val body = response.body?.string()
//
//            Log.d("UploadImageUseCase", "HTTP Code: ${response.code}")
//            Log.d("UploadImageUseCase", "HTTP Message: ${response.message}")
//            Log.d("UploadImageUseCase", "HTTP Response Body: $body")
//
//            // check if the response is successful and body is not null
//            if (!response.isSuccessful || body == null) {
//                return UploadImageResult.Error("HTTP ${response.code} - ${response.message}. Body null? ${body == null}")
//            }
//
//            // Parse the response converting string to a JSON object
//            val json = JSONObject(body)
//            val success = json.optBoolean("success", false)
//
//
//            // Check if the upload was successful and dictates output for method based on success or failure
//            if (success) {
//                val imageUrl = json.getJSONObject("data").getString("link")
//                UploadImageResult.Success(imageUrl)
//            } else {
//                val errorMsg = json.optJSONObject("data")?.optString("error", "Unknown error") ?: "Unknown error"
//                UploadImageResult.Error(errorMsg)
//            }
//
//        } catch (e: Exception) {
//            Log.e("UploadImage", "Exception during image upload", e) // FULL exception details
//            UploadImageResult.Error("Exception: ${e.localizedMessage ?: e.toString()}")
//        }
//    }
    suspend fun execute(imageBytes: ByteArray): UploadImageResult {
        return withContext(Dispatchers.IO) {
            try {
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                val requestBody = FormBody.Builder()
                    .add("image", base64Image)
                    .add("type", "base64")
                    .build()

                val request = Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID ${IMGUR_CLIENT_ID.trim()}")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                Log.d("UploadImageUseCase", "HTTP Code: ${response.code}")
                Log.d("UploadImageUseCase", "HTTP Message: ${response.message}")
                Log.d("UploadImageUseCase", "HTTP Response Body: $body")

                if (!response.isSuccessful || body == null) {
                    return@withContext UploadImageResult.Error("HTTP ${response.code} - ${response.message}. Body null? ${body == null}")
                }

                val json = JSONObject(body)
                val success = json.optBoolean("success", false)

                if (success) {
                    val imageUrl = json.getJSONObject("data").getString("link")
                    UploadImageResult.Success(imageUrl)
                } else {
                    val errorMsg = json.optJSONObject("data")?.optString("error", "Unknown error") ?: "Unknown error"
                    UploadImageResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e("UploadImage", "Exception during image upload", e)
                UploadImageResult.Error("Exception: ${e.localizedMessage ?: e.toString()}")
            }
        }
    }
}
