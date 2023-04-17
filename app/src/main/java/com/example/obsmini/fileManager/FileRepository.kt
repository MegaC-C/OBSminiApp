package com.example.obsmini.fileManager

import android.content.Context
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

interface FileRepository {
    suspend fun uploadTrack(url: String, token: String, file: File): Boolean
}

class FileRepositoryImpl @Inject constructor(
    private val context: Context,
    private val fileApi: FileUploadApi
) : FileRepository {

    override suspend fun uploadTrack(url: String, token: String, file: File): Boolean {
        return try {
            val response = fileApi.uploadTrack(
                url = url,
                token = token,
//                title = MultipartBody.Part
//                    .createFormData(
//                        "title",
//                        "title"
//                    ),
//                description = MultipartBody.Part
//                    .createFormData(
//                        "description",
//                        "description"
//                    ),
                track = MultipartBody.Part
                    .createFormData(
                        "body",
                        file.name,
                        file.asRequestBody("text/csv".toMediaTypeOrNull())
                    )
            )
            if (response.isSuccessful) {
                Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show()
                return true
            } else {
                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch(e: IOException) {
            Toast.makeText(context, "Upload Failed: check Internet", Toast.LENGTH_SHORT).show()
            false
        } catch (e: HttpException) {
            Toast.makeText(context, "HTTP Error: ${e.code()}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
