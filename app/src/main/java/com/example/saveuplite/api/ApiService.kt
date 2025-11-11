package com.example.saveuplite.api

import com.example.saveuplite.model.Usuario
import com.example.saveuplite.model.dto.UsuarioLoginDTO
import com.example.saveuplite.model.dto.UsuarioRegistroDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz que define todos los endpoints de la API para Retrofit.
 */
interface ApiService {

    /**
     * Registra un nuevo usuario en el backend.
     *
     * @param usuarioDTO El objeto con los datos de registro del usuario.
     * @return Una respuesta del servidor. Un 201 CREATED si es exitoso.
     */
    @POST("api/usuarios/register")
    suspend fun registerUsuario(@Body usuarioDTO: UsuarioRegistroDTO): Response<Void>

    /**
     * Autentica a un usuario contra el backend.
     *
     * @param loginDTO El objeto con las credenciales de login.
     * @return Una respuesta del servidor con el objeto Usuario si es exitoso.
     */
    @POST("api/usuarios/login")
    suspend fun loginUsuario(@Body loginDTO: UsuarioLoginDTO): Response<Usuario>
}
