package com.example.transportes_sumapaz.data.remote.model

data class CrearLiderRequest(
    val usuario: String,
    val nombre: String,
    val contrasena: String,
    val nivel: Int
)

data class CrearLiderResponse(
    val success: Boolean,
    val message: String?,
    val error: String?
)
