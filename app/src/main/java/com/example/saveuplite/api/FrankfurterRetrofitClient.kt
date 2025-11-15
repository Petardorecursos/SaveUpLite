package com.example.saveuplite.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente de Retrofit configurado para conectarse a la API de Frankfurter.app.
 */
object FrankfurterRetrofitClient {

    // URL base de la API de Frankfurter
    private const val BASE_URL = "https://api.frankfurter.app/"

    // Creación de la instancia de Retrofit
    val instance: FrankfurterApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(FrankfurterApiService::class.java)
    }
}
