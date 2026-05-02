package com.example.oskolki.network

import com.example.oskolki.model.LoginRequest
import com.example.oskolki.model.LoginResponse
import com.example.oskolki.model.Marker
import com.example.oskolki.model.MarkerDetail
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: LoginRequest): LoginResponse

    @GET("api/fragments")
    suspend fun getFragments(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int
    ): List<Marker>

    @GET("api/fragments/{id}")
    suspend fun getFragmentDetail(@Path("id") id: String): MarkerDetail
}
