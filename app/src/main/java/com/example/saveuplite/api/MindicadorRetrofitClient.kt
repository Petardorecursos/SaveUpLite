package com.example.saveuplite.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente de Retrofit configurado para conectarse a la API de Mindicador.cl.
 */
object MindicadorRetrofitClient {

    // URL base de la API de Mindicador
    private const val BASE_URL = "https://mindicador.cl/"

    // Configuración de Gson para parsear las fechas correctamente
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    // Creación de la instancia de Retrofit
    val instance: MindicadorApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit.create(MindicadorApiService::class.java)
    }
}
