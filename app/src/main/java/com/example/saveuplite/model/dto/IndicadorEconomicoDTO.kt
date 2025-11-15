package com.example.saveuplite.model.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Data Transfer Object (DTO) para representar un único indicador económico
 * que se recibe de la API de mindicador.cl.
 */
data class IndicadorEconomicoDTO(
    @SerializedName("codigo")
    val codigo: String, // ej: "dolar"

    @SerializedName("nombre")
    val nombre: String, // ej: "Dólar observado"

    @SerializedName("unidad_medida")
    val unidadMedida: String, // ej: "Pesos"

    @SerializedName("fecha")
    val fecha: Date,

    @SerializedName("valor")
    val valor: Double
)
