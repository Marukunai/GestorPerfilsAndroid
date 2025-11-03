package com.example.gestorperfils // Assegura't que el package sigui el correcte

import android.view.View
import android.widget.TextView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.text.Normalizer

/**
 * Funció personalitzada per fer la cerca de text ignorant accents i majúscules/minúscules.
 */
fun withTextIgnoringAccents(text: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        // Normalitzem el text esperat (sense accents ni majúscules)
        private val normalizedExpectedText = Normalizer
            .normalize(text, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()

        override fun describeTo(description: Description) {
            description.appendText("with text value: $text (ignoring accents and case)")
        }

        override fun matchesSafely(view: View): Boolean {
            if (view !is TextView) return false

            // Obtenim el text de la vista actual i el normalitzem
            val actualText = view.text.toString()
            val normalizedActualText = Normalizer
                .normalize(actualText, Normalizer.Form.NFD)
                .replace("\\p{Mn}+".toRegex(), "")
                .lowercase()

            // Compareu els textos normalitzats
            return normalizedActualText == normalizedExpectedText
        }
    }
}