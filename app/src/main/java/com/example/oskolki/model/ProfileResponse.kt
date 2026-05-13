package com.example.oskolki.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("avatar_url")
    val avatarUrl: String?
)

data class ProfileUpdateRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("avatar_url")
    val avatarUrl: String?
)

data class Achievement(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("icon_url")
    val iconUrl: String?,

    @SerializedName("unlocked")
    val unlocked: Boolean = false,

    @SerializedName("lat")
    val latitude: Double? = null,

    @SerializedName("lng")
    val longitude: Double? = null
)

data class GoogleAuthRequest(
    @SerializedName("code")
    val code: String,

    @SerializedName("state")
    val state: String
)

data class GoogleAuthUrlResponse(
    @SerializedName("url")
    val url: String
)

data class GoogleAuthAndroidRequest(
    @SerializedName("idToken")
    val idToken: String
)