package com.example.oskolki.model

import com.google.gson.annotations.SerializedName

data class FoundFragmentIdsResponse(
    @SerializedName("fragment_ids")
    val ids: List<String>
)