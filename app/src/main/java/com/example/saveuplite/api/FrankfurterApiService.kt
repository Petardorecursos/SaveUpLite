package com.example.saveuplite.api

import com.example.saveuplite.model.dto.ConversionResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints de la API de Frankfurter.app para Retrofit.
 */
interface FrankfurterApiService {

    /**
     * Obtiene la lista de todas las monedas soportadas por la API.
     * La URL completa será: https://api.frankfurter.app/currencies
     * @return Un mapa donde la clave es el código de la moneda (ej: "USD") y el valor es el nombre (ej: "United States Dollar").
     */
    @GET("currencies")
    suspend fun getCurrencies(): Response<Map<String, String>>

    /**
     * Realiza una conversión de moneda desde una base a una o más monedas de destino.
     * La URL completa será: https://api.frankfurter.app/latest?amount=10&from=USD&to=CLP
     * @param amount El monto a convertir.
     * @param from El código de la moneda de origen (ej: "USD").
     * @param to El código de la moneda de destino (ej: "CLP").
     * @return Un objeto [ConversionResponseDTO] con los detalles de la conversión.
     */
    @GET("latest")
    suspend fun getLatestConversion(
        @Query("amount") amount: Double,
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ConversionResponseDTO>

}
