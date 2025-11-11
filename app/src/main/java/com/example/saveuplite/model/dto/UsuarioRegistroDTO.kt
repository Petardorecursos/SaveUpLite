package com.example.saveuplite.model.dto

/**
 * Data Transfer Object para registrar un nuevo usuario.
 * Este objeto coincide con el que espera el endpoint /api/usuarios/register del backend.
 */
data class UsuarioRegistroDTO(
    val rut: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val contrasena: String
)
