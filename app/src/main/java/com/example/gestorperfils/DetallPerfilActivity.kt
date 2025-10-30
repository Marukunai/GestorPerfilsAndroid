package com.example.gestorperfils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetallPerfilActivity : AppCompatActivity() {

    // Vistes per visualització (ara seran EditTexts/TextViews que canviaran)
    private lateinit var tvDetallNom: TextView
    private lateinit var etDetallEmail: EditText // Canvi a EditText per edició
    private lateinit var etDetallImatgeUrl: EditText // NOU: Per la URL de la imatge
    private lateinit var etDetallEstat: EditText // Canvi a EditText per edició

    private lateinit var btnCanviarEstat: Button
    private lateinit var btnEliminarPerfil: Button
    private lateinit var btnGuardarCanvis: Button // NOU BOTÓ DE GUARDAR

    // Estat
    private var perfilActual: PerfilUsuari? = null // Per emmagatzemar el perfil actual
    private var isEditing: Boolean = false // Estat per controlar si estem editant

    // Claus de Comunicació (Ampliació del companion object)
    companion object {
        const val PERFIL_SELECCIONAT_KEY = "PERFIL_SELECCIONAT"
        const val RESULT_ELIMINAR = 200 // Codi de resultat per a l'eliminació
        const val EXTRA_PERFIL_ID_ELIMINAR = "perfil_id_eliminar" // Clau per l'ID a eliminar

        // CLAU NECESSÀRIA per retornar el perfil modificat a LlistaPerfilsActivity
        const val EXTRA_PERFIL_MODIFICAT_KEY = "com.example.gestorperfils.PERFIL_MODIFICAT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detall_perfil)

        // 1. Inicialitzar les vistes
        inicialitzarVistes()

        // 2. Recuperar l'objecte PerfilUsuari
        perfilActual = recuperarPerfil() // <--- CRIDA RESOLTA

        // 3. Si tenim dades, les mostrem i configurem
        perfilActual?.let {
            mostrarDades(it)
            configurarBotoEstat()
            configurarBotoEliminar()
            configurarBotoGuardar() // <-- Nova funció de guardar
        }

        // 4. Configurar la Toolbar i el Menu
        configurarToolbar()
        actualitzarEstatEdicio(false) // Comencem en mode visualització
    }

    // --- Lògica del Menu (Toolbar) ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detall, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_editar -> {
                toggleEdicio() // Canvia entre mode visualització i edició
                true
            }
            android.R.id.home -> {
                // Gestió de la fletxa de retorn de la Toolbar (Botó HOME)
                onSupportNavigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Al tornar enrere, tornem el resultat d'una simple visualització (RESULT_CANCELED)
        setResult(RESULT_CANCELED)
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // --- Funcions d'Inicialització ---

    private fun configurarToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detall del Perfil"
        }
    }

    private fun inicialitzarVistes() {
        tvDetallNom = findViewById(R.id.tvDetallNom) // Aquesta es manté TextView (Nom i Cognom)
        etDetallEmail = findViewById(R.id.etDetallEmail) // Vistes canviades a EditText
        etDetallImatgeUrl = findViewById(R.id.etDetallImatgeUrl) // NOU
        etDetallEstat = findViewById(R.id.etDetallEstat) // Vistes canviades a EditText

        btnCanviarEstat = findViewById(R.id.btnCanviarEstat)
        btnEliminarPerfil = findViewById(R.id.btnEliminarPerfil)
        btnGuardarCanvis = findViewById(R.id.btnGuardarCanvis) // Assumim que aquest botó existeix al layout
    }

    /**
     * Recupera l'objecte PerfilUsuari de l'Intent. (FUNCIÓ AFAGIDA)
     */
    private fun recuperarPerfil(): PerfilUsuari? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(PERFIL_SELECCIONAT_KEY, PerfilUsuari::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(PERFIL_SELECCIONAT_KEY)
        }
    }

    /**
     * Omple els TextViews/EditTexts amb la informació del perfil.
     */
    private fun mostrarDades(perfil: PerfilUsuari) {
        // Nom i Cognom es mantenen no editables
        tvDetallNom.text = "${perfil.nom} ${perfil.cognom} (${perfil.edat} anys)"

        // Camps editables
        etDetallEmail.setText(perfil.email)
        etDetallImatgeUrl.setText(perfil.imatge_url) // NOU
        etDetallEstat.setText(if (perfil.actiu) "Actiu" else "Inactiu")
    }

    // --- Lògica d'Edició ---

    /**
     * Commuta l'estat d'edició.
     */
    private fun toggleEdicio() {
        isEditing = !isEditing
        actualitzarEstatEdicio(isEditing)
    }

    /**
     * Habilita/Deshabilita els camps per a l'edició.
     */
    private fun actualitzarEstatEdicio(editing: Boolean) {
        // Habilita/Deshabilita l'edició als EditTexts
        etDetallEmail.isEnabled = editing
        etDetallImatgeUrl.isEnabled = editing
        etDetallEstat.isEnabled = editing // Es podria reemplaçar amb un Switch/Spinner per control més estricte

        // Canvia la visibilitat dels botons segons el mode
        btnCanviarEstat.visibility = if (editing) Button.GONE else Button.VISIBLE
        btnEliminarPerfil.visibility = if (editing) Button.GONE else Button.VISIBLE
        btnGuardarCanvis.visibility = if (editing) Button.VISIBLE else Button.GONE

        // Actualitza el títol
        supportActionBar?.title = if (editing) "Editant Perfil" else "Detall del Perfil"
    }

    // --- Lògica de Botons ---

    /**
     * Configura el listener del botó per canviar l'estat.
     */
    private fun configurarBotoEstat() {
        btnCanviarEstat.setOnClickListener {
            val nouEstat = if (perfilActual?.actiu == true) false else true

            // Creem una còpia actualitzada del perfil per actualitzar la UI
            perfilActual = perfilActual?.copy(actiu = nouEstat)

            perfilActual?.let { p ->
                // Actualitzem l'EditText (UI)
                etDetallEstat.setText(if (p.actiu) "Actiu" else "Inactiu")
                // Retornem el canvi sense entrar en mode edició
                retornarCanvis(p)
            }
        }
    }

    /**
     * Configura el listener del botó per guardar els canvis.
     */
    private fun configurarBotoGuardar() {
        btnGuardarCanvis.setOnClickListener {
            guardarICrearResultat()
        }
    }

    /**
     * Recull les dades dels camps editats i retorna el resultat a LlistaPerfilsActivity.
     */
    private fun guardarICrearResultat() {
        val nouEmail = etDetallEmail.text.toString()
        val novaImatgeUrl = etDetallImatgeUrl.text.toString()
        val nouEstatString = etDetallEstat.text.toString().lowercase()

        // 1. Validació bàsica (permetem que l'estat sigui només "actiu" o "inactiu")
        val nouEstatBoolean = when (nouEstatString) {
            "actiu" -> true
            "inactiu" -> false
            else -> {
                Toast.makeText(this, "Estat no vàlid. Utilitza 'Actiu' o 'Inactiu'.", Toast.LENGTH_SHORT).show()
                return // Sortim de la funció si la validació falla
            }
        }

        // 2. Creació del nou objecte PerfilUsuari (utilitzant 'copy' per preservar l'ID, el Nom, Cognom, Edat)
        val perfilModificat = perfilActual?.copy(
            email = nouEmail,
            actiu = nouEstatBoolean,
            imatge_url = novaImatgeUrl
        )

        perfilModificat?.let {
            retornarCanvis(it) // Retorna el perfil modificat a l'Activity anterior
        }
    }

    /**
     * Retorna el perfil actualitzat a l'Activity de llista.
     */
    private fun retornarCanvis(perfil: PerfilUsuari) {
        val resultIntent = Intent().apply {
            // Adjuntem l'objecte PerfilUsuari modificat
            putExtra(EXTRA_PERFIL_MODIFICAT_KEY, perfil)
        }
        setResult(RESULT_OK, resultIntent) // RESULT_OK indica modificació exitosa
        finish()
    }


    /**
     * Configura el listener del botó per eliminar el perfil i retornar el resultat.
     */
    private fun configurarBotoEliminar() {
        btnEliminarPerfil.setOnClickListener {
            val idPerfil = perfilActual?.id

            if (idPerfil != null) {
                // 1. Crear l'Intent de resultat i adjuntar l'ID
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_PERFIL_ID_ELIMINAR, idPerfil)
                }

                // 2. Establir el codi de resultat personalitzat per a ELIMINAR
                setResult(RESULT_ELIMINAR, resultIntent)

                // 3. Tancar la pantalla
                finish()
            }
        }
    }
}