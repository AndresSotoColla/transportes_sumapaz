package com.example.transportes_sumapaz.data.remote.model

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val id: String? = null,
    val updated: Int? = null,
    val error: String? = null
)

data class ParticipantesResponse(
    val success: Boolean,
    val participantes: List<Participante>,
    val error: String? = null
)

data class ViajesResponse(
    val success: Boolean,
    val viajes: List<Viaje>,
    val error: String? = null
)
