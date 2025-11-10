package com.example.saveuplite

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.saveuplite.model.Post
import com.example.saveuplite.repository.PostRepository
import com.example.saveuplite.ui.screens.postScreen.PostScreen
import com.example.saveuplite.viewmodel.PostViewModel
import org.junit.Rule
import org.junit.Test

class PostScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun el_titulo_de_post_debe_aparecer_en_pantalla() {
        // Simulamos los datos que el ViewModel entregaría
        val fakePosts = listOf(
            Post(userId = 1, id = 1, title = "Título 1", body = "Contenido 1"),
            Post(userId = 2, id = 2, title = "Título 2", body = "Contenido 2")
        )

        // Creamos un repositorio falso que devuelve datos de prueba
        val fakeRepository = object : PostRepository() {
            override suspend fun getPosts(): List<Post> {
                return fakePosts
            }
        }

        // Instanciamos el ViewModel REAL con el repositorio falso
        val viewModel = PostViewModel(fakeRepository)

        // Renderizamos el PostScreen con el ViewModel
        composeRule.setContent {
            PostScreen(viewModel = viewModel)
        }

        // Esperamos a que la UI se actualice después de la carga asíncrona de datos
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule
                .onAllNodesWithText("Título: Título 1", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Validamos que los títulos se muestran correctamente en la UI
        composeRule.onNodeWithText(text = "Título: Título 1").assertIsDisplayed()
        composeRule.onNodeWithText(text = "Título: Título 2").assertIsDisplayed()
    }
}
