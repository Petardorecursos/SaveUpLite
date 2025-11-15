package com.example.saveuplite.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object (DTO) para representar la respuesta de una conversión de moneda
 * desde la API de Frankfurter.app.
 */
data class ConversionResponseDTO(
    @SerializedName("amount")
    val amount: Double,

    @SerializedName("base")
    val base: String, // La moneda de origen, ej: "USD"

    @SerializedName("date")
    val date: String,

    // `rates` es un objeto que contiene la moneda de destino como clave y su valor como resultado.
    // Ej: "rates": {"CLP": 925.49}
    @SerializedName("rates")
    val rates: Map<String, Double>
)
