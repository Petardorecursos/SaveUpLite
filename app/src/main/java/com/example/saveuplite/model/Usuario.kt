package com.example.saveuplite.model

import java.util.Date

/**
 * Representa el modelo de un usuario en la aplicación.
 *
 * Esta clase es una adaptación del modelo de backend para ser utilizada
 * de forma sencilla en la app de Android, conteniendo solo los datos del usuario.
 * La lógica de negocios (como iniciar sesión o validar) se manejará en otras
 * capas de la arquitectura de la app (ViewModels, Repositories).
 */
data class Usuario(
    val rut: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val contrasena: String?, // La contraseña ahora es nullable para coincidir con la respuesta de la API
    val fechaRegistro: Date
)
