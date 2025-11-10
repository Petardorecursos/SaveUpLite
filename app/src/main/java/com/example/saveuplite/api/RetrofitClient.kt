package com.example.saveuplite.api

// Importa la interfaz ApiService del paquete 'remote'
import com.example.saveuplite.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto singleton para crear y gestionar la instancia de Retrofit.
 */
object RetrofitClient {

    // URL base del backend.
    // Si ejecutas el backend en tu misma máquina y usas el emulador de Android,
    // 10.0.2.2 es la dirección que apunta al localhost de tu computadora.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Creación de la instancia de Retrofit usando un inicializador "lazy".
    // Esto asegura que la instancia se cree solo una vez, la primera vez que se accede a ella.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para convertir JSON
            .build()
    }

    // Expone públicamente la implementación de la interfaz ApiService del paquete 'remote'.
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
