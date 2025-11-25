package com.example.saveuplite.model.deuda

data class DeudaCreacion(
    val nombre: String,
    val descripcion: String?,
    val montoTotal: Double,
    val cantidadCuotas: Int,
    val usuarioRut: String
)
