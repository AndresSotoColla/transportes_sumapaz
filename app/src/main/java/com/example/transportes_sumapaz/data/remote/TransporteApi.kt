package com.example.transportes_sumapaz.data.remote

import com.example.transportes_sumapaz.data.remote.model.BaseResponse
import com.example.transportes_sumapaz.data.remote.model.CerrarViajeRequest
import com.example.transportes_sumapaz.data.remote.model.CrearLiderRequest
import com.example.transportes_sumapaz.data.remote.model.CrearLiderResponse
import com.example.transportes_sumapaz.data.remote.model.IniciarViajeRequest
import com.example.transportes_sumapaz.data.remote.model.LoginRequest
import com.example.transportes_sumapaz.data.remote.model.LoginResponse
import com.example.transportes_sumapaz.data.remote.model.ParticipantesResponse
import com.example.transportes_sumapaz.data.remote.model.ViajesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TransporteApi {

    @GET("api/transporte_router.php?route=/participantes")
    suspend fun getParticipantes(): Response<ParticipantesResponse>

    @POST("api/transporte_router.php?route=/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/transporte_router.php?route=/lideres/crear")
    suspend fun crearLider(@Body request: CrearLiderRequest): Response<CrearLiderResponse>

    @GET("api/transporte_router.php?route=/viajes")
    suspend fun getViajes(): Response<ViajesResponse>

    @POST("api/transporte_router.php?route=/asistencia/iniciar")
    suspend fun iniciarAsistencia(@Body request: IniciarViajeRequest): Response<BaseResponse>

    @POST("api/transporte_router.php?route=/asistencia/cerrar")
    suspend fun cerrarAsistencia(@Body request: CerrarViajeRequest): Response<BaseResponse>
}
