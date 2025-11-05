package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.model.Post
import com.example.saveuplite.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel que mantiene el estado de los datos obtenidos
class PostViewModel : ViewModel() {

    private val repository = PostRepository()

    // Flujo mutable que contiene la lista de posts
    private val _postList = MutableStateFlow<List<Post>>(value = emptyList())

    // Flujo público de solo lectura
    val postList: StateFlow<List<Post>> = _postList

    // Se llama automáticamente al iniciar
    init {
        fetchPosts()
    }

    // Función que obtiene los datos en segundo plano
    private fun fetchPosts() {
        viewModelScope.launch {
            try {
                _postList.value = repository.getPosts()
            } catch (e: Exception) {
                println("Error al obtener datos: ${e.localizedMessage}")
            }
        }
    }
}
