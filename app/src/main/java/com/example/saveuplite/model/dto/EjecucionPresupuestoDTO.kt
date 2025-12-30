package com.example.saveuplite.model.dto

data class EjecucionPresupuestoDTO(
    val presupuestoNecesidades: Double = 0.0,
    val gastoNecesidades: Double = 0.0,
    val presupuestoDeseos: Double = 0.0,
    val gastoDeseos: Double = 0.0,
    val totalIngresos: Double = 0.0,
    val porcentajeNecesidadesConfigurado: Double = 50.0,
    val porcentajeDeseosConfigurado: Double = 30.0
)
