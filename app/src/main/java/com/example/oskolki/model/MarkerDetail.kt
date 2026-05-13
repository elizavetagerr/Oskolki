package com.example.oskolki.model

import com.google.gson.annotations.SerializedName

data class MarkerDetail(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("lat")
    val latitude: Double,

    @SerializedName("lng")
    val longitude: Double,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("photo_urls")
    val photoUrls: List<String>? = null,

    @SerializedName("sound_url")
    val audioUrl: String? = null,

    @SerializedName("expires_at")
    val expiresAt: String? = null
)