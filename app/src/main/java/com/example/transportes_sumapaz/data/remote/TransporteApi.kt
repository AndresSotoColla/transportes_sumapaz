package com.example.transportes_sumapaz.data.remote

import com.example.transportes_sumapaz.data.remote.model.BaseResponse
import com.example.transportes_sumapaz.data.remote.model.ParticipantesResponse
import com.example.transportes_sumapaz.data.remote.model.ViajesResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface TransporteApi {

    @GET("transporte_router.php/participantes")
    suspend fun getParticipantes(): Response<ParticipantesResponse>

    @GET("transporte_router.php/viajes")
    suspend fun getViajes(): Response<ViajesResponse>

    @FormUrlEncoded
    @POST("transporte_router.php/asistencia/iniciar")
    suspend fun iniciarAsistencia(
        @Field("id_viaje") idViaje: String,
        @Field("numero_documento") numeroDocumento: String
    ): Response<BaseResponse>
    
    @FormUrlEncoded
    @POST("transporte_router.php/asistencia/cerrar")
    suspend fun cerrarAsistencia(
        @Field("id_viaje") idViaje: String,
        @Field("numero_documento") numeroDocumento: String
    ): Response<BaseResponse>
}
