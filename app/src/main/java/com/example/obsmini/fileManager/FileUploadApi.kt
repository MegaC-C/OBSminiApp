package com.example.obsmini.fileManager

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


// TODO add description and title to request body, this way it does not work..

interface FileUploadApi {

    @POST
    @Multipart
    suspend fun uploadTrack(
        @retrofit2.http.Url url: String,
        @Header("Authorization") token: String,
//        @Part title: MultipartBody.Part,
//        @Part description: MultipartBody.Part,
        @Part track: MultipartBody.Part
    ): Response<Unit>
}