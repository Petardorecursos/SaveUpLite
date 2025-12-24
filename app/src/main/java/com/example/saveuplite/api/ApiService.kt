package com.example.saveuplite.api

import com.example.saveuplite.model.Usuario
import com.example.saveuplite.model.deuda.Deuda
import com.example.saveuplite.model.deuda.DeudaCreacion
import com.example.saveuplite.model.deuda.PagoDeuda
import com.example.saveuplite.model.dto.*
import com.example.saveuplite.model.meta.AbonoRetiro
import com.example.saveuplite.model.meta.MetaAhorro
import com.example.saveuplite.model.meta.MetaAhorroCreacion
import retrofit2.Response
import retrofit2.http.*

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

    @GET("api/movimientos/paginados/usuario/{rut}")
    suspend fun obtenerMovimientosPaginados(
        @Path("rut") rut: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageResponseDTO<MovimientoResponseDTO>>

    // --- Endpoints de Categorías ---
    @GET("api/categorias")
    suspend fun getCategorias(): Response<List<CategoriaDTO>>

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

    // --- Endpoints de Metas de Ahorro ---
    @POST("api/metas")
    suspend fun crearMeta(@Body meta: MetaAhorroCreacion): Response<MetaAhorro>

    @GET("api/metas/usuario/{rut}")
    suspend fun obtenerMetas(@Path("rut") rut: String): Response<List<MetaAhorro>>

    @POST("api/metas/{metaId}/abonar")
    suspend fun abonarAMeta(
        @Path("metaId") metaId: Long,
        @Body abono: AbonoRetiro
    ): Response<MetaAhorro>

    @POST("api/metas/{metaId}/retirar")
    suspend fun retirarDeMeta(
        @Path("metaId") metaId: Long,
        @Body retiro: AbonoRetiro
    ): Response<MetaAhorro>

    @PUT("api/metas/{metaId}")
    suspend fun editarMeta(
        @Path("metaId") metaId: Long,
        @Body meta: MetaAhorroCreacion
    ): Response<MetaAhorro>

    @DELETE("api/metas/{metaId}")
    suspend fun eliminarMeta(@Path("metaId") metaId: Long): Response<Void>
}
