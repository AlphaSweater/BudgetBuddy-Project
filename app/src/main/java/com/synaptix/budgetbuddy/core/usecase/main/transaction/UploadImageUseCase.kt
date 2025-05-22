package com.synaptix.budgetbuddy.core.usecase.main.transaction

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import org.json.JSONObject

//imgur client id
private const val IMGUR_CLIENT_ID = "c3363df746ecd23"


class UploadImageUseCase {
    sealed class UploadImageResult {
        data class Success(val imageUrl: String) : UploadImageResult()
        data class Error(val message: String) : UploadImageResult()
    }

    private val client = OkHttpClient()


    suspend fun execute(imageBytes: ByteArray): UploadImageResult {
        return try{
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            //builds request body for imgur
            val requestBody = FormBody.Builder()
                .add("image", base64Image)
                .build()

            //builds Http request for imgur
            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .addHeader("Authorization", "Client-ID $IMGUR_CLIENT_ID")
                .post(requestBody)
                .build()

            // Execute the request
            val response = client.newCall(request).execute()
            val body = response.body?.string()

            // check if the response is successful and body is not null
            if (!response.isSuccessful || body == null) {
                return UploadImageResult.Error("Failed to upload image: ${response.message}")
            }

            // Parse the response converting string to a JSON object
            val json = JSONObject(body)
            val success = json.optBoolean("success", false)


            // Check if the upload was successful and dictates output for method based on success or failure
            if (success) {
                val imageUrl = json.getJSONObject("data").getString("link")
                UploadImageResult.Success(imageUrl)
            } else {
                val errorMsg = json.optJSONObject("data")?.optString("error", "Unknown error") ?: "Unknown error"
                UploadImageResult.Error(errorMsg)
            }

        } catch (e: Exception) {
            UploadImageResult.Error("Exception: ${e.message}")
        }
    }
}
