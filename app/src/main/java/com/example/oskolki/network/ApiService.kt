package com.example.oskolki.network

import com.example.oskolki.model.Achievement
import com.example.oskolki.model.FoundFragmentIdsResponse
import com.example.oskolki.model.FragmentResponse
import com.example.oskolki.model.GoogleAuthAndroidRequest
import com.example.oskolki.model.GoogleAuthRequest
import com.example.oskolki.model.GoogleAuthUrlResponse
import com.example.oskolki.model.LoginRequest
import com.example.oskolki.model.LoginResponse
import com.example.oskolki.model.Marker
import com.example.oskolki.model.MarkerDetail
import com.example.oskolki.model.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: LoginRequest): LoginResponse

    @GET("api/auth/google/url")
    suspend fun getGoogleAuthUrl(@Query("state") state: String): GoogleAuthUrlResponse

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: GoogleAuthRequest): LoginResponse

    @POST("api/auth/google/android")
    suspend fun googleAuthAndroid(@Body request: GoogleAuthAndroidRequest): LoginResponse

    @GET("api/fragments")
    suspend fun getFragments(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int
    ): List<Marker>

    @GET("api/fragments/{id}")
    suspend fun getFragmentDetail(@Path("id") id: String): MarkerDetail

    @Multipart
    @POST("api/fragments")
    suspend fun createFragment(
        @Part("text") text: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part photos: List<MultipartBody.Part>?,
        @Part sound: MultipartBody.Part?
    ): FragmentResponse

    @POST("api/fragments/{id}/found")
    suspend fun markFragmentFound(@Path("id") id: String)

    @GET("api/fragments/found")
    suspend fun getFoundFragmentIds(): FoundFragmentIdsResponse

    @GET("api/users/profile")
    suspend fun getProfile(): ProfileResponse

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): ProfileResponse

    @Multipart
    @PUT("api/users/profile")
    suspend fun updateProfile(
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): ProfileResponse

    @GET("api/achievements")
    suspend fun getAchievements(): List<Achievement>

    @GET("api/achievements/mine")
    suspend fun getMyAchievements(): List<Achievement>
}
