package com.example.saveuplite.model

import java.util.Date

/**
 * Representa un movimiento de saldo (ingreso, gasto, etc.) para un usuario.
 *
 * Esta clase es una adaptación del modelo de backend para ser utilizada
 * en la base de datos local de la app.
 */
data class Saldo(
    val idSaldo: Int,
    val monto: Float,
    val fechaRegistro: Date,
    val usuarioRut: String, // Clave foránea al RUT del usuario en la tabla de autenticación
    val tipoEvento: EventoSaldo
)

/**
 * Define el tipo de evento que generó el movimiento de saldo.
 */
enum class EventoSaldo {
    INGRESO, GASTO, META_AHORRO, DEUDA_PAGADA, AJUSTE_MANUAL, REVISIÓN_MENSUAL, DEUDA
}
