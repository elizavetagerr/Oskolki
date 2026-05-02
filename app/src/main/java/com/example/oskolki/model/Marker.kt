package com.example.oskolki.model

import com.google.gson.annotations.SerializedName

data class Marker(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("text")
    val text: String?,
    
    @SerializedName("lat")
    val latitude: Double,
    
    @SerializedName("lng")
    val longitude: Double,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)
