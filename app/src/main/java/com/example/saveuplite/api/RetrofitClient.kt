package com.example.saveuplite.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto singleton para crear y gestionar la instancia de Retrofit.
 */
object RetrofitClient {

    // URL base del backend desplegado en Render.
    // private const val BASE_URL = "https://lite-backend-1wn9.onrender.com/"
    // --- ¡IMPORTANTE! ---
    // Para testear con un backend local en un dispositivo físico,
    // reemplaza "TU_IP_LOCAL" con la dirección IPv4 de tu computador.
    // Ejemplo: "http://192.168.1.105:8080/"
     private const val BASE_URL = "http://192.168.18.8:8080/"
    //private const val BASE_URL = "https://lite-backend-1wn9.onrender.com/"

    // Creación de la instancia de Retrofit usando un inicializador "lazy".
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para convertir JSON
            .build()
    }

    // Expone públicamente la implementación de la interfaz ApiService.
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
