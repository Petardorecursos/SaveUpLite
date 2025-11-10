package com.example.saveuplite.remote

import com.example.saveuplite.model.Post
import com.example.saveuplite.model.Usuario
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interfaz que define todos los endpoints de la API para Retrofit.
 */
interface ApiService {

    /**
     * Obtiene una lista de posts.
     */
    @GET("posts")
    suspend fun getPosts(): List<Post>

    /**
     * Obtiene un usuario por su RUT desde el backend.
     *
     * @param rut El RUT del usuario a buscar.
     * @return El objeto Usuario.
     */
    @GET("api/usuariosK/{rut}")
    suspend fun getUsuarioByRut(@Path("rut") rut: String): Usuario
}
