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

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\
    // Executes the operation to upload an image to Imgur
    suspend fun execute(imageBytes: ByteArray): UploadImageResult {
        return withContext(Dispatchers.IO) {
            try {
                //converts bytearray to base64 as required by Imgur API
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                //creates the https request body
                val requestBody = FormBody.Builder()
                    .add("image", base64Image)
                    .add("type", "base64")
                    .build()

                //creates the request with the Imgur API endpoint and authorization header
                val request = Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID ${IMGUR_CLIENT_ID.trim()}")
                    .post(requestBody)
                    .build()

                //executes the request and retrieves the response
                val response = client.newCall(request).execute()
                val body = response.body?.string()

                //logs the response details for debugging
                Log.d("UploadImageUseCase", "HTTP Code: ${response.code}")
                Log.d("UploadImageUseCase", "HTTP Message: ${response.message}")
                Log.d("UploadImageUseCase", "HTTP Response Body: $body")

                //checks if the response was successful and if the body is not null
                if (!response.isSuccessful || body == null) {
                    return@withContext UploadImageResult.Error("HTTP ${response.code} - ${response.message}. Body null? ${body == null}")
                }

                //parses the JSON response to extract the image URL
                val json = JSONObject(body)
                val success = json.optBoolean("success", false)

                // checks if the upload was successful
                if (success) {
                    val imageUrl = json.getJSONObject("data").getString("link")
                    UploadImageResult.Success(imageUrl)
                } else {
                    val errorMsg = json.optJSONObject("data")?.optString("error", "Unknown error") ?: "Unknown error"
                    UploadImageResult.Error(errorMsg)
                }

            // handles any exceptions that occur during the upload process
            } catch (e: Exception) {
                Log.e("UploadImage", "Exception during image upload", e)
                UploadImageResult.Error("Exception: ${e.localizedMessage ?: e.toString()}")
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\