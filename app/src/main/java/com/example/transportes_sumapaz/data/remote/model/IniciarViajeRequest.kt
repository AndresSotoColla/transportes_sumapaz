package com.example.transportes_sumapaz.data.remote.model

data class IniciarViajeRequest(
    val id_viaje: String,
    val cedula_pasajero: String,
    val nombre_conductor: String,
    val placa_vehiculo: String,
    val tipo_vehiculo: String,
    val hora_inicio: String,
    val hora_inicio_dispositivo: String,
    val coordenadas_inicio: String,
    val estado: String
)
