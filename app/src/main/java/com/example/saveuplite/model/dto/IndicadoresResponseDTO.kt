package com.example.saveuplite.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) para representar la respuesta completa de la API de mindicador.cl.
 * Contiene varios indicadores económicos, cada uno representado por un `IndicadorEconomicoDTO`.
 * Las claves del JSON (uf, dolar, etc.) son dinámicas, por lo que las mapeamos explícitamente.
 */
data class IndicadoresResponseDTO(
    @SerializedName("uf")
    val uf: IndicadorEconomicoDTO,

    @SerializedName("dolar")
    val dolar: IndicadorEconomicoDTO,

    @SerializedName("dolar_intercambio")
    val dolarIntercambio: IndicadorEconomicoDTO,

    @SerializedName("euro")
    val euro: IndicadorEconomicoDTO,

    @SerializedName("ipc")
    val ipc: IndicadorEconomicoDTO,

    @SerializedName("utm")
    val utm: IndicadorEconomicoDTO,

    @SerializedName("ivp")
    val ivp: IndicadorEconomicoDTO,

    @SerializedName("imacec")
    val imacec: IndicadorEconomicoDTO
)
