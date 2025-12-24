package com.example.saveuplite.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // --- ¡IMPORTANTE! Elige la URL base correcta descomentando la línea que necesites ---

    // 1. Para testear en el EMULADOR de Android Studio:
    // private const val BASE_URL = "http://10.0.2.2:8080/"

    // 2. Para testear en un DISPOSITIVO FÍSICO (asegúrate de que tu PC y el móvil estén en la misma red WiFi):
    //private const val BASE_URL = "http://192.168.18.8:8080/"

    // 3. Para producción (backend en Render):
    private const val BASE_URL = "https://lite-backend-1wn9.onrender.com/"

    // --- Configuración de Gson para el formato de fecha estándar ---
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Usa el Gson personalizado
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
