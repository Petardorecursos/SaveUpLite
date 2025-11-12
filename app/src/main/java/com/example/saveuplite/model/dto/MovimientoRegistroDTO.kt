package com.example.saveuplite.model.dto

import com.example.saveuplite.model.enums.TipoMovimiento
import com.google.gson.annotations.SerializedName

/**
 * DTO para enviar la información de un nuevo movimiento al backend.
 */
data class MovimientoRegistroDTO(
    val monto: Double,
    val descripcion: String,
    val tipoMovimiento: TipoMovimiento,
    val usuarioRut: String,

    // Opcionales, para futuras implementaciones
    val deudaId: Long? = null,
    val metaId: Long? = null
)
