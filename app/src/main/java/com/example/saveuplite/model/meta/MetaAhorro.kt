package com.example.saveuplite.model.meta

import java.util.Date

/**
 * Representa el estado completo de una meta de ahorro, tal como se recibe del backend.
 */
data class MetaAhorro(
    val id: Long,
    val nombre: String,
    val montoObjetivo: Double?,
    val fechaLimite: Date?,
    val montoActual: Double
)
