package com.example.saveuplite.ui.screens.postScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saveuplite.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(viewModel: PostViewModel) {
    // obervamos el flujo de datos del viewModel
    val posts = viewModel.postList.collectAsState().value
    //scaffold con TopAppBar
    // CONTINUAR DESDE PASO 7 GUIA 3.1.2
}
