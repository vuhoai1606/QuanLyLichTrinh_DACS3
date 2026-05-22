package com.bfy.schedule_app.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AmbientSoundDto(
    val id: String,
    val name: String,
    val url: String,
    val category: String
)
