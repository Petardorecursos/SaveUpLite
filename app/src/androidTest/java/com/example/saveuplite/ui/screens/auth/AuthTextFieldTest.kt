package com.example.saveuplite.ui.screens.auth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.saveuplite.ui.theme.SaveupLITETheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authTextField_DisplaysCorrectly_InNormalState() {
        // 1. Arrange: Preparamos el componente a probar
        composeTestRule.setContent {
            SaveupLITETheme {
                AuthTextField(
                    value = "test@example.com",
                    onValueChange = {},
                    label = "Email",
                    icon = Icons.Outlined.Email,
                    isError = false,
                    errorText = null
                )
            }
        }

        // 2. Assert: Verificamos que los elementos correctos se muestren
        // El label "Email" debe ser visible
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()

        // El valor "test@example.com" debe ser visible
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun authTextField_DisplaysError_WhenInErrorState() {
        val errorMessage = "Formato de correo inválido"

        // 1. Arrange: Preparamos el componente en estado de error
        composeTestRule.setContent {
            SaveupLITETheme {
                AuthTextField(
                    value = "invalid-email",
                    onValueChange = {},
                    label = "Email",
                    icon = Icons.Outlined.Email,
                    isError = true,
                    errorText = errorMessage
                )
            }
        }

        // 2. Assert: Verificamos que el mensaje de error se muestre
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}