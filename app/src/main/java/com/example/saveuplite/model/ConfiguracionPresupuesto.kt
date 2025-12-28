package com.example.saveuplite.model

import com.google.gson.annotations.SerializedName

data class ConfiguracionPresupuesto(
    val id: Long,
    val porcentajeNecesidades: Double,
    val porcentajeDeseos: Double,
    val porcentajeAhorro: Double,
    val activo: Boolean
)
