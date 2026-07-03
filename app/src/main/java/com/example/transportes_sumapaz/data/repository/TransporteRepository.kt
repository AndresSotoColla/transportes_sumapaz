package com.example.transportes_sumapaz.data.repository

import com.example.transportes_sumapaz.data.remote.TransporteApi
import com.example.transportes_sumapaz.data.remote.model.BaseResponse
import com.example.transportes_sumapaz.data.remote.model.ViajeConPasajeros
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class TransporteRepository @Inject constructor(
    private val api: TransporteApi
) {

    private fun <T> handleNetworkResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Cuerpo de respuesta nulo"))
            }
        } else {
            val errorJson = response.errorBody()?.string()
            val errorMessage = try {
                val baseError = Gson().fromJson(errorJson, BaseResponse::class.java)
                baseError.error ?: "Error desconocido"
            } catch (e: Exception) {
                "Error de red: ${response.code()}"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun obtenerViajesConPasajeros(): Result<List<ViajeConPasajeros>> = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                // Hacer las llamadas en paralelo para mayor velocidad
                val viajesDeferred = async { api.getViajes() }
                val participantesDeferred = async { api.getParticipantes() }

                val viajesResponse = viajesDeferred.await()
                val participantesResponse = participantesDeferred.await()

                val viajesResult = handleNetworkResponse(viajesResponse)
                val participantesResult = handleNetworkResponse(participantesResponse)

                if (viajesResult.isSuccess && participantesResult.isSuccess) {
                    val viajes = viajesResult.getOrThrow().viajes
                    val participantes = participantesResult.getOrThrow().participantes

                    // Convertimos la lista a un mapa para búsquedas O(1)
                    val participantesMap = participantes.associateBy { it.numero_documento }

                    // Cruce de datos
                    val viajesEnriquecidos = viajes.map { viaje ->
                        val pasajerosCompletos = viaje.pasajeros.mapNotNull { cedula ->
                            participantesMap[cedula]
                        }

                        ViajeConPasajeros(
                            viaje = viaje,
                            listaPasajeros = pasajerosCompletos
                        )
                    }

                    Result.success(viajesEnriquecidos)
                } else {
                    val errorMsg = viajesResult.exceptionOrNull()?.message
                        ?: participantesResult.exceptionOrNull()?.message
                        ?: "Error al obtener los datos cruzados"
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: com.example.transportes_sumapaz.data.remote.model.LoginRequest): Result<com.example.transportes_sumapaz.data.remote.model.LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(request)
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarAsistencia(request: com.example.transportes_sumapaz.data.remote.model.IniciarViajeRequest): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.iniciarAsistencia(request)
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cerrarAsistencia(request: com.example.transportes_sumapaz.data.remote.model.CerrarViajeRequest): Result<BaseResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.cerrarAsistencia(request)
            handleNetworkResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
