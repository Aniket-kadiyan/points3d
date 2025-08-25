package com.example.points3d.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("points/status")
    suspend fun evaluateStatus(@Body req: StatusRequest): StatusResponse

    @GET("models/{modelNo}")
    suspend fun getModelBundle(@Path("modelNo") modelNo: String): ModelBundle
}