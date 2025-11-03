import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.gestorperfils.withTextIgnoringAccents
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import androidx.test.espresso.action.ViewActions.swipeLeft
import com.example.gestorperfils.LlistaPerfilsActivity
import com.example.gestorperfils.PerfilsAdapter
import com.example.gestorperfils.R

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class LlistaPerfilsTest {

    // Regla per iniciar LlistaPerfilsActivity abans de cada test
    @get:Rule
    val activityRule = ActivityScenarioRule(LlistaPerfilsActivity::class.java)

    private val NOM_PERFIL_TEST = "Anna García"
    // La teva llista s'ha de carregar des de JSON, així que assumirem que hi ha elements.
    // L'element a eliminar serà el primer (POSICIÓ 0).

    // -----------------------------------------------------------------
    // TEST 'A': Eliminació amb Cancel·lació (Ha d'anar primer, no modifica l'estat)
    // -----------------------------------------------------------------
    @Test
    fun a_testSwipeToDeleteAmbCancelacioMantePerfil() {
        // ASSUMPCIÓ CLAU: El primer perfil (ítem a posició 0) té el nom correcte.

        // 1. Lliscament cap a l'esquerra del primer ítem
        onView(withId(R.id.rvPerfils))
            .perform(RecyclerViewActions.actionOnItemAtPosition<PerfilsAdapter.PerfilViewHolder>(0, swipeLeft()))

        // 2. VERIFICACIÓ: Comprova que el diàleg de confirmació apareix
        onView(withText("Confirmar Eliminació")).check(matches(isDisplayed()))

        // 3. Clicar el botó de cancel·lació "Cancel·lar"
        onView(withText("Cancel·lar")).perform(click())

        // 4. VERIFICACIÓ FINAL: Comprova que l'ítem ENCARA és visible a la llista
        onView(withTextIgnoringAccents(NOM_PERFIL_TEST)).check(matches(isDisplayed()))    }

    // -----------------------------------------------------------------
    // TEST 'B': Eliminació amb Confirmació (Ha d'anar segon, ja que modifica l'estat)
    // -----------------------------------------------------------------
    @Test
    fun b_testSwipeToDeleteAmbConfirmacioEliminaPerfil() {
        // ASSUMPCIÓ CLAU: El primer perfil (ítem a posició 0) té el nom correcte.

        // 1. Lliscament cap a l'esquerra del primer ítem
        onView(withId(R.id.rvPerfils))
            .perform(RecyclerViewActions.actionOnItemAtPosition<PerfilsAdapter.PerfilViewHolder>(0, swipeLeft()))

        // 2. VERIFICACIÓ: Comprova que el diàleg de confirmació apareix
        onView(withText("Confirmar Eliminació")).check(matches(isDisplayed()))

        // 3. Clicar el botó de confirmació "Eliminar"
        onView(withText("Eliminar")).perform(click())

        // 4. VERIFICACIÓ FINAL: Comprova que l'ítem ja NO és visible a la llista
        onView(withTextIgnoringAccents(NOM_PERFIL_TEST)).check(doesNotExist())    }
}