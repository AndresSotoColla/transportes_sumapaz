package com.example.transportes_sumapaz.data.remote.model

data class CerrarViajeRequest(
    val id_viaje: String,
    val cedula_pasajero: String,
    val hora_fin_dispositivo: String,
    val coordenadas_fin: String,
    val estado: String
)
