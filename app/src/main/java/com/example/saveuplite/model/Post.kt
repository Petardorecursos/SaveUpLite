package com.example.saveuplite.model

data class Post(
    val userId: Int, // ID del usuario que crea el post
    val id: Int, // ID del post
    val title: String, // titulo del post
    val body: String //cuerpo o contenido del post
)

