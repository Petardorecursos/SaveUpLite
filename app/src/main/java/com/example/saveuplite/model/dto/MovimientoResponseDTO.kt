package com.example.saveuplite.model.dto

import com.example.saveuplite.model.enums.TipoMovimiento
import java.util.Date

/**
 * DTO para recibir la información de un movimiento desde el backend.
 */
data class MovimientoResponseDTO(
    val id: Long,
    val monto: Double,
    val descripcion: String,
    val fecha: Date,
    val tipoMovimiento: TipoMovimiento
)
