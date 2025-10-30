package com.example.gestorperfils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NouPerfilActivity : AppCompatActivity() {

    // Clau per retornar el nou perfil a la llista
    companion object {
        const val EXTRA_NOU_PERFIL = "nou_perfil_creat"
        // Utilitzarem una ID temporal per als nous perfils, ja que no estem en una BBDD real
        private var nextId = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nou_perfil)

        // Configuració de la fletxa de retorn a la Toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Crear Nou Perfil"
        }

        val btnGuardar = findViewById<Button>(R.id.btnGuardarNouPerfil)
        btnGuardar.setOnClickListener {
            guardarNouPerfil()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun guardarNouPerfil() {
        // 1. Recuperar dades dels EditTexts
        val etNom = findViewById<EditText>(R.id.etNom)
        val etCognom = findViewById<EditText>(R.id.etCognom)
        val etEdat = findViewById<EditText>(R.id.etEdat)
        val etEmail = findViewById<EditText>(R.id.etEmail)

        // Validació simple (només comprovació de buit per simplicitat)
        if (etNom.text.isBlank() || etCognom.text.isBlank() || etEdat.text.isBlank() || etEmail.text.isBlank()) {
            // Aquí es mostraria un Toast o un missatge d'error
            return
        }

        // 2. Crear l'objecte PerfilUsuari (utilitzant el teu model Parcelable)
        val nouPerfil = PerfilUsuari(
            id = nextId++, // Assigna ID temporal i incrementa
            nom = etNom.text.toString(),
            cognom = etCognom.text.toString(),
            edat = etEdat.text.toString().toIntOrNull() ?: 0,
            email = etEmail.text.toString(),
            imatge_url = "" // Deixem buit per ara
        )

        // 3. Crear l'Intent i adjuntar el nou perfil
        val resultIntent = Intent().apply {
            putExtra(EXTRA_NOU_PERFIL, nouPerfil)
        }

        // 4. Retornar el resultat a l'Activity anterior (LlistaPerfilsActivity)
        setResult(RESULT_OK, resultIntent)
        finish() // Tanca la pantalla actual
    }
}