package com.example.saveuplite.model.deuda

import java.util.Date

data class Deuda(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val montoTotal: Double,
    val cantidadCuotas: Int,
    val estado: EstadoDeuda,
    val fechaCreacion: Date,
    val montoPagado: Double,
    val montoRestante: Double,
    val cuotasPagadas: Int
)
