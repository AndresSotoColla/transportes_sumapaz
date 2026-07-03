package com.example.transportes_sumapaz.data.remote.model

data class LoginResponse(
    val success: Boolean,
    val lider: Lider? = null
)

data class Lider(
    val usuario: String,
    val nombre: String,
    val debe_cambiar_contrasena: Int,
    val nivel: Int
)
