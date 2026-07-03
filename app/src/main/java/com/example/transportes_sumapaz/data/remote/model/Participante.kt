package com.example.transportes_sumapaz.data.remote.model

import com.google.gson.annotations.SerializedName

data class Participante(
    val numero_documento: String,
    val nombres: String,
    val apellidos: String,
    val telefono: String? = null
)

data class Viaje(
    val id: String,
    val fecha_viaje: String,
    val ruta: String,
    val estado: String,
    val pasajeros: List<String> = emptyList(),
    val asistencias: List<Asistencia> = emptyList()
)

data class Asistencia(
    val id: String,
    val id_viaje: String,
    val numero_documento_participante: String,
    val estado: String,
    val hora_registro: String
)

data class ViajeConPasajeros(
    val viaje: Viaje,
    val listaPasajeros: List<Participante>
)
