package com.example.saveuplite.model.meta

import java.util.Date

/**
 * DTO para enviar la información para crear/editar una meta de ahorro al backend.
 */
data class MetaAhorroCreacion(
    val nombre: String,
    val montoObjetivo: Double?,
    val fechaLimite: Date?,
    val usuarioRut: String
)
