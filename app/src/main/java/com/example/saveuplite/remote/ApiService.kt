package com.example.saveuplite.remote

import com.example.saveuplite.model.Post
import retrofit2.http.GET
// esta interfaz define los endpoints HTTP

interface ApiService {
    // define una solicitud GET al endpoint /posts
    @GET("posts")
    // esta función devuelve una lista de objetos Post
    suspend fun getPosts(): List<Post>
}