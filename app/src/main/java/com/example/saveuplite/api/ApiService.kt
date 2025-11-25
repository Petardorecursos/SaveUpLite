package com.example.saveuplite.api

import com.example.saveuplite.model.Usuario
import com.example.saveuplite.model.deuda.Deuda
import com.example.saveuplite.model.deuda.DeudaCreacion
import com.example.saveuplite.model.deuda.PagoDeuda
import com.example.saveuplite.model.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz que define todos los endpoints de la API para Retrofit.
 */
interface ApiService {

    // --- Endpoints de Usuarios ---

    @POST("api/usuarios/register")
    suspend fun registerUsuario(@Body usuarioDTO: UsuarioRegistroDTO): Response<Void>

    @POST("api/usuarios/login")
    suspend fun loginUsuario(@Body loginDTO: UsuarioLoginDTO): Response<Usuario>

    // --- Endpoints de Movimientos y Saldos ---

    @POST("api/movimientos")
    suspend fun registrarMovimiento(@Body movimientoDTO: MovimientoRegistroDTO): Response<MovimientoResponseDTO>

    @GET("api/movimientos/usuario/{rut}")
    suspend fun obtenerMovimientosPorUsuario(
        @Path("rut") rut: String,
        @Query("limit") limit: Int? = null
    ): Response<List<MovimientoResponseDTO>>

    @GET("api/saldos/{rut}")
    suspend fun obtenerSaldoActual(@Path("rut") rut: String): Response<SaldoDTO>

    // --- ¡NUEVO ENDPOINT! ---
    @GET("api/movimientos/paginados/usuario/{rut}")
    suspend fun obtenerMovimientosPaginados(
        @Path("rut") rut: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageResponseDTO<MovimientoResponseDTO>>

    // --- Endpoints de Deudas ---

    @POST("api/deudas")
    suspend fun crearDeuda(@Body deudaCreacion: DeudaCreacion): Response<Deuda>

    @GET("api/deudas/usuario/{rut}")
    suspend fun obtenerDeudasPorUsuario(@Path("rut") rut: String): Response<List<Deuda>>

    @POST("api/deudas/{deudaId}/pagar")
    suspend fun registrarPagoDeuda(
        @Path("deudaId") deudaId: Long,
        @Body pagoDeuda: PagoDeuda
    ): Response<Deuda>

    @PUT("api/deudas/{deudaId}")
    suspend fun editarDeuda(
        @Path("deudaId") deudaId: Long,
        @Body deudaCreacion: DeudaCreacion
    ): Response<Deuda>

    @PATCH("api/deudas/{deudaId}/cancelar")
    suspend fun cancelarDeuda(@Path("deudaId") deudaId: Long): Response<Deuda>
}
