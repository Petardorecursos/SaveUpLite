package com.example.saveuplite.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.*

class NumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.trim()
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val number = originalText.toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)

        val format = NumberFormat.getNumberInstance(Locale("es", "CL"))
        format.maximumFractionDigits = 0
        val formattedText = format.format(number)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Mapeo simple: ajusta el cursor basado en cuántos puntos se añadieron
                val dotsBeforeCursor = formattedText.take(offset + (formattedText.length - originalText.length)).count { it == '.' }
                return offset + dotsBeforeCursor
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Mapeo inverso: quita los puntos para encontrar la posición original
                val dotsBeforeCursor = formattedText.take(offset).count { it == '.' }
                return offset - dotsBeforeCursor
            }
        }

        return TransformedText(
            AnnotatedString(formattedText),
            offsetMapping
        )
    }
}
