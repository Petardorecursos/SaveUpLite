package com.example.saveuplite.api

import com.example.saveuplite.model.Usuario // <-- ESTA ES LA LÍNEA CORREGIDA
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interfaz que define todos los endpoints de la API para Retrofit.
 */
interface ApiService {

    /**
     * Obtiene un usuario por su RUT desde el backend.
     *
     * @param rut El RUT del usuario a buscar.
     * @return El objeto Usuario.
     */
    @GET("api/usuariosK/{rut}")
    suspend fun getUsuarioByRut(@Path("rut") rut: String): Usuario

    /**
     * Registra un nuevo usuario en el backend.
     *
     * @param usuario El objeto Usuario a registrar.
     * @return Una respuesta vacía para indicar éxito o fracaso.
     */
    @POST("api/usuariosK/register")
    suspend fun registerUsuario(@Body usuario: Usuario): Response<Void>
}

