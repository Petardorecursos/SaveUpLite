package com.example.saveuplite.model

data class ConfiguracionPresupuestoDTO(
    val porcentajeNecesidades: Double,
    val porcentajeDeseos: Double,
    val porcentajeAhorro: Double,
    val activo: Boolean,
    val asignaciones: List<AsignacionPresupuestoDTO> = emptyList()
)

data class AsignacionPresupuestoDTO(
    val metaId: Long,
    val porcentaje: Double
)
