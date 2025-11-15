package com.example.saveuplite.api

import com.example.saveuplite.model.dto.IndicadoresResponseDTO
import retrofit2.Response
import retrofit2.http.GET

/**
 * Interfaz que define los endpoints de la API de Mindicador.cl para Retrofit.
 */
interface MindicadorApiService {

    /**
     * Obtiene los principales indicadores económicos del día.
     * La URL completa será: https://mindicador.cl/api
     */
    @GET("api")
    suspend fun getIndicadoresEconomicos(): Response<IndicadoresResponseDTO>

}
