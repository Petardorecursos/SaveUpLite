package com.example.saveuplite.model.dto

data class CategoriaDTO(
    val id: Long,
    val nombre: String,
    val iconId: String?,
    val colorHex: String?,
    val tipoPresupuesto: com.example.saveuplite.model.enums.TipoPresupuesto? = null
)
