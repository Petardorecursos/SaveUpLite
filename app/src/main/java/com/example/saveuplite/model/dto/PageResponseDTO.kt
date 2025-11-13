package com.example.saveuplite.model.dto

/**
 * DTO genérico para recibir respuestas paginadas del backend.
 */
data class PageResponseDTO<T>(
    val content: List<T>,
    val currentPage: Int,
    val totalItems: Long,
    val totalPages: Int
)
