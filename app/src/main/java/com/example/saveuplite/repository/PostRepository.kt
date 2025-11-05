package com.example.saveuplite.repository

import com.example.saveuplite.model.Post
import com.example.saveuplite.remote.RetrofitInstance

// este repositorio se encarga de acceder a los datos usando Retrofit

class PostRepository {
    suspend fun getPosts(): List<Post> {
        return RetrofitInstance.api.getPosts()
    }
}