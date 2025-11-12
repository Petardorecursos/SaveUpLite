package com.example.saveuplite.model.enums

/**
 * Enum que representa las categorías de los movimientos financieros.
 * Debe coincidir con el Enum del backend.
 */
enum class TipoMovimiento {
    INGRESO_GENERAL,
    GASTO_GENERAL,
    PAGO_DEUDA,
    ABONO_META,
    RETIRO_META
}
