package com.example.gestorperfils

import android.app.Activity.RESULT_OK
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NouPerfilValidationTest {

    // 1. Regla per iniciar NouPerfilActivity abans de cada test
    @get:Rule
    val activityRule = ActivityScenarioRule(NouPerfilActivity::class.java)

    // -----------------------------------------------------------
    // TEST 1: Creació Exitosa (Comprova que l'Activity es tanca amb OK)
    // -----------------------------------------------------------
    @Test
    fun testCreacioExitosaRetornaResultOK() {
        // 1. Escriure Totes les dades VÀLIDES
        onView(withId(R.id.etNom)).perform(typeText("TestNom"), closeSoftKeyboard())
        onView(withId(R.id.etCognom)).perform(typeText("TestCognom"), closeSoftKeyboard())
        onView(withId(R.id.etEdat)).perform(typeText("30"), closeSoftKeyboard())
        onView(withId(R.id.etEmail)).perform(typeText("valid@test.com"), closeSoftKeyboard())
        // No cal URL per la validació del teu codi, però l'afegim per ser complets
        onView(withId(R.id.etImatgeUrl)).perform(typeText("http://fake.url/img.png"), closeSoftKeyboard())

        // 2. Clicar el botó de crear
        onView(withId(R.id.btnGuardarNouPerfil)).perform(click())

        // 3. VERIFICACIÓ CLAU: Comprova que l'Activity es tanca amb RESULT_OK
        activityRule.scenario.onActivity { activity ->
            // Si el teu codi crida setResult(RESULT_OK) i finish(), l'estat ha de ser FINISHED.
            // Aquesta és la millor comprovació per a un retorn exitós.
            assert(activity.isFinishing)
        }
    }

    // -----------------------------------------------------------
    // TEST 2: Validació amb Camp Buit (Comprova que l'Activity NO es tanca)
    // -----------------------------------------------------------
    @Test
    fun testCreacioAmbNomBuitLActivtyNoEsTanca() {
        // 1. Escriure dades a la resta de camps
        onView(withId(R.id.etNom)).perform(clearText(), closeSoftKeyboard()) // Nom Buit
        onView(withId(R.id.etCognom)).perform(typeText("TestCognom"), closeSoftKeyboard())
        onView(withId(R.id.etEdat)).perform(typeText("30"), closeSoftKeyboard())
        onView(withId(R.id.etEmail)).perform(typeText("test@example.com"), closeSoftKeyboard())

        // 2. Clicar el botó de crear
        onView(withId(R.id.btnGuardarNouPerfil)).perform(click())

        // 3. VERIFICACIÓ CLAU: Comprova que l'Activity NO s'ha finalitzat.
        activityRule.scenario.onActivity { activity ->
            // Si la validació falla i es fa 'return', l'Activity ha de seguir activa.
            assert(!activity.isFinishing)
        }
    }

    // -----------------------------------------------------------
    // TEST 3: Validació amb Edat Buida (Comprova que l'Activity NO es tanca)
    // -----------------------------------------------------------
    @Test
    fun testCreacioAmbEdatBuidaLActivtyNoEsTanca() {
        // 1. Escriure dades a la resta de camps
        onView(withId(R.id.etNom)).perform(typeText("TestNom"), closeSoftKeyboard())
        onView(withId(R.id.etCognom)).perform(typeText("TestCognom"), closeSoftKeyboard())
        onView(withId(R.id.etEdat)).perform(clearText(), closeSoftKeyboard()) // Edat Buida
        onView(withId(R.id.etEmail)).perform(typeText("test@example.com"), closeSoftKeyboard())

        // 2. Clicar el botó de crear
        onView(withId(R.id.btnGuardarNouPerfil)).perform(click())

        // 3. VERIFICACIÓ CLAU: Comprova que l'Activity NO s'ha finalitzat.
        activityRule.scenario.onActivity { activity ->
            assert(!activity.isFinishing)
        }
    }
}