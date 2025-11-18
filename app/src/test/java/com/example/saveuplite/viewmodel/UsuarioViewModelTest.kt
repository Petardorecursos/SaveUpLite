package com.example.saveuplite.viewmodel

import android.app.Application
import com.example.saveuplite.api.ApiService
import com.example.saveuplite.model.Usuario
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.util.Date

@ExperimentalCoroutinesApi
class UsuarioViewModelTest : BehaviorSpec({

    // Regla para reemplazar el Dispatcher.Main por uno de prueba
    val testDispatcher = UnconfinedTestDispatcher()

    // Mocks para las dependencias
    lateinit var mockApiService: ApiService
    lateinit var mockApplication: Application
    lateinit var viewModel: UsuarioViewModel

    // Se ejecuta antes de cada bloque `given`
    beforeContainer {
        Dispatchers.setMain(testDispatcher)
        mockApiService = mockk()
        mockApplication = mockk(relaxed = true) // relaxed para no mockear todos los métodos de Application
        viewModel = UsuarioViewModel(mockApiService, mockApplication)
    }

    // Se ejecuta después de cada bloque `given`
    afterContainer {
        Dispatchers.resetMain()
    }

    given("un intento de login") {

        `when`("las credenciales son válidas y la API responde exitosamente") {
            val email = "test@test.com"
            val contrasena = "password123"
            val mockUser = Usuario("12345678-9", "Test", "User", email, "", Date())
            val successResponse = Response.success(mockUser)

            // Configurar el mock: cuando se llame a loginUsuario, devolver la respuesta exitosa
            coEvery { mockApiService.loginUsuario(any()) } returns successResponse

            // Llamar a la función que queremos probar
            viewModel.login(email, contrasena)

            then("el estado de la UI debe reflejar la autenticación exitosa") {
                val state = viewModel.uiState.value
                state.isAuthenticated shouldBe true
                state.currentUser shouldBe mockUser
                state.isLoading shouldBe false
                state.errorMessage shouldBe null
            }
        }

        `when`("las credenciales son inválidas y la API responde con error 401") {
            val email = "wrong@test.com"
            val contrasena = "wrongpassword"
            val errorResponse = Response.error<Usuario>(401, "".toResponseBody(null))

            // Configurar el mock para que devuelva el error 401
            coEvery { mockApiService.loginUsuario(any()) } returns errorResponse

            // Llamar a la función
            viewModel.login(email, contrasena)

            then("el estado de la UI debe mostrar el mensaje de error correspondiente") {
                val state = viewModel.uiState.value
                state.isAuthenticated shouldBe false
                state.currentUser shouldBe null
                state.isLoading shouldBe false
                state.errorMessage shouldBe "Credenciales inválidas. Verifica tu email y contraseña."
            }
        }
    }
})
