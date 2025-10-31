package com.example.gestorperfils

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetallPerfilActivity : AppCompatActivity() {

    // Vistes per visualització (ara seran EditTexts/TextViews que canviaran)
    private lateinit var ivFotoPerfilDetall: ImageView
    private lateinit var tvDetallNom: TextView
    private lateinit var etDetallEmail: EditText // Canvi a EditText per edició
    private lateinit var etDetallImatgeUrl: EditText // NOU: Per la URL de la imatge
    private lateinit var swDetallEstat: SwitchCompat

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
        perfilActual = recuperarPerfil()

        // 3. Si tenim dades, les mostrem i configurem
        perfilActual?.let {
            mostrarDades(it)
            carregarImatge(it.imatge_url)
            configurarBotoEliminar()
            configurarBotoGuardar()
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
                onSupportNavigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // --- Funcions d'Inicialització ---

    private fun configurarToolbar() {
        // 💡 Assumim que la toolbar es carrega manualment si el tema és NoActionBar
        // Si la crida al findViewById falta aquí, cal afegir-la:
        // val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetall)
        // setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detall del Perfil"
        }
    }

    private fun inicialitzarVistes() {
        ivFotoPerfilDetall = findViewById(R.id.ivFotoPerfilDetall)
        tvDetallNom = findViewById(R.id.tvDetallNom)
        etDetallEmail = findViewById(R.id.etDetallEmail)
        etDetallImatgeUrl = findViewById(R.id.etDetallImatgeUrl)
        swDetallEstat = findViewById(R.id.swDetallEstat)

        btnEliminarPerfil = findViewById(R.id.btnEliminarPerfil)
        btnGuardarCanvis = findViewById(R.id.btnGuardarCanvis)
    }

    private fun recuperarPerfil(): PerfilUsuari? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(PERFIL_SELECCIONAT_KEY, PerfilUsuari::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(PERFIL_SELECCIONAT_KEY)
        }
    }

    private fun mostrarDades(perfil: PerfilUsuari) {
        // Nom i Cognom es mantenen no editables
        tvDetallNom.text = "${perfil.nom} ${perfil.cognom} (${perfil.edat} anys)"

        // Camps editables
        etDetallEmail.setText(perfil.email)
        etDetallImatgeUrl.setText(perfil.imatge_url)
        swDetallEstat.isChecked = perfil.actiu
    }

    // Càrrega de la Imatge amb Glide
    private fun carregarImatge(url: String) {
        Log.d("DetallPerfilActivity", "Carregant imatge des de: $url")

        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(ivFotoPerfilDetall)
        } else {
            ivFotoPerfilDetall.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    // --- Lògica d'Edició ---

    private fun toggleEdicio() {
        isEditing = !isEditing
        actualitzarEstatEdicio(isEditing)
    }

    private fun actualitzarEstatEdicio(editing: Boolean) {
        // Habilita/Deshabilita l'edició
        etDetallEmail.isEnabled = editing
        etDetallImatgeUrl.isEnabled = editing
        swDetallEstat.isEnabled = editing

        // Canvia la visibilitat dels botons segons el mode
        btnEliminarPerfil.visibility = if (editing) Button.GONE else Button.VISIBLE
        btnGuardarCanvis.visibility = if (editing) Button.VISIBLE else Button.GONE

        // Actualitza el títol
        supportActionBar?.title = if (editing) "Editant Perfil" else "Detall del Perfil"
    }

    // --- Lògica de Botons i Diàlegs ---

    private fun configurarBotoGuardar() {
        btnGuardarCanvis.setOnClickListener {
            guardarICrearResultat()
        }
    }

    private fun guardarICrearResultat() {
        val nouEmail = etDetallEmail.text.toString()
        val novaImatgeUrl = etDetallImatgeUrl.text.toString()
        val nouEstatBoolean = swDetallEstat.isChecked

        // Creació del nou objecte PerfilUsuari
        val perfilModificat = perfilActual?.copy(
            email = nouEmail,
            actiu = nouEstatBoolean,
            imatge_url = novaImatgeUrl
        )

        perfilModificat?.let {
            retornarCanvis(it) // Retorna el perfil modificat a l'Activity anterior
        }
    }

    private fun retornarCanvis(perfil: PerfilUsuari) {
        val resultIntent = Intent().apply {
            // Adjuntem l'objecte PerfilUsuari modificat
            putExtra(EXTRA_PERFIL_MODIFICAT_KEY, perfil)
        }
        setResult(RESULT_OK, resultIntent) // RESULT_OK indica modificació exitosa
        finish()
    }


    /**
     * Configura el listener del botó per eliminar el perfil. Mostra el diàleg.
     */
    private fun configurarBotoEliminar() {
        btnEliminarPerfil.setOnClickListener {
            // 💡 En lloc d'eliminar directament, cridem el diàleg de confirmació
            mostrarDialegConfirmacioEliminacio()
        }
    }

    /**
     * Mostra un AlertDialog per confirmar l'eliminació.
     */
    private fun mostrarDialegConfirmacioEliminacio() {
        // Obtenim el nom per al missatge
        val nomComplet = "${perfilActual?.nom} ${perfilActual?.cognom}"

        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminació")
            .setMessage("Estàs segur que vols eliminar el perfil de $nomComplet?")
            .setPositiveButton("Eliminar") { dialog, which ->
                // Si l'usuari confirma, executa l'eliminació i tanca
                eliminarPerfilIRetornarResultat()
            }
            .setNegativeButton("Cancel·lar", null) // Tanca el diàleg sense fer res
            .show()
    }

    /**
     * Funció que executa l'eliminació real i retorna el resultat.
     */
    private fun eliminarPerfilIRetornarResultat() {
        val idPerfil = perfilActual?.id

        if (idPerfil != null) {
            val resultIntent = Intent().apply {
                putExtra(EXTRA_PERFIL_ID_ELIMINAR, idPerfil)
            }
            // Establir el codi de resultat personalitzat per a ELIMINAR
            setResult(RESULT_ELIMINAR, resultIntent)
            finish()
        } else {
            Toast.makeText(this, "Error: No s'ha pogut obtenir la ID del perfil.", Toast.LENGTH_SHORT).show()
        }
    }
}