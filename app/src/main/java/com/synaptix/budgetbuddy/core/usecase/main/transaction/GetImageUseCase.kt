package com.synaptix.budgetbuddy.core.usecase.main.transaction

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class GetImageUseCase {

    suspend fun execute(imageUrl: String): Result<Bitmap> {
        return withContext(Dispatchers.IO){
            try{
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    Result.success(bitmap)
                } else {
                    Result.failure(IOException("Failed to load image"))
                }
            }
            catch (e: Exception){
                Result.failure(e)
            }
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~EOF~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\\