package com.example.gestorperfils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog // NOU
import android.util.Log // NOU
import com.google.gson.Gson // NOU
import com.google.gson.reflect.TypeToken // NOU
import java.io.IOException // NOU
import java.io.File // NOU

class MenuPrincipalActivity : AppCompatActivity() {

    private val FITXER_DADES = "dades_usuaris.json" // El nom del fitxer de persistència

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        val btnAnarALlista = findViewById<Button>(R.id.btnAnarALlista)
        val btnRestaurarDades = findViewById<Button>(R.id.btnRestaurarDades) // NOU

        btnAnarALlista.setOnClickListener {
            val intent = Intent(this, LlistaPerfilsActivity::class.java)
            startActivity(intent)
        }

        // 💡 LÒGICA DE RESTAURACIÓ DE DADES
        btnRestaurarDades.setOnClickListener {
            mostrarDialegConfirmacioRestauracio()
        }
    }

    // -----------------------------------------------------------------
    // Lògica del JSON i Persistència (Movuda de LlistaPerfilsActivity)
    // -----------------------------------------------------------------

    /**
     * Funció auxiliar que llegeix *només* el JSON d'Assets.
     */
    private fun carregarDadesDesDeAssets(): List<PerfilUsuari> {
        val listType = object : TypeToken<List<PerfilUsuari>>() {}.type
        val jsonString: String? = try {
            Log.d("JSON_RESET", "Llegint dades mestres des d'Assets.")
            assets.open("dades_usuaris.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            Log.e("JSON_RESET", "Error llegint des d'Assets: ${ioException.message}")
            return emptyList()
        }

        return jsonString?.let { Gson().fromJson<List<PerfilUsuari>>(it, listType) } ?: emptyList()
    }

    /**
     * Restaura completament les dades sobreescrivint el fitxer intern amb el contingut dels Assets.
     */
    private fun restaurarDadesCompletament() {
        val perfilsMestres = carregarDadesDesDeAssets()

        try {
            val jsonString = Gson().toJson(perfilsMestres)

            // openFileOutput és un mètode d'Activity per obrir fitxers interns
            openFileOutput(FITXER_DADES, MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
            Log.i("JSON_RESET", "Dades restaurades completament des d'Assets.")

            // Opcional: Mostrar un missatge a l'usuari
            AlertDialog.Builder(this)
                .setTitle("Restauració Completada")
                .setMessage("S'han restaurat ${perfilsMestres.size} perfils. Les dades guardades s'han sobreescrit.")
                .setPositiveButton("D'acord", null)
                .show()

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("JSON_SAVE", "Error guardant dades restaurades: ${e.message}")
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("No s'han pogut restaurar les dades.")
                .setPositiveButton("D'acord", null)
                .show()
        }
    }

    /**
     * Mostra un AlertDialog per confirmar la restauració completa de dades.
     */
    private fun mostrarDialegConfirmacioRestauracio() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Restauració")
            .setMessage("ATENCIÓ: Vols sobreescriure totes les dades guardades amb les dades inicials (Assets)? Aquesta acció és irreversible.")
            .setPositiveButton("Sí, Restaurar") { dialog, which ->
                restaurarDadesCompletament()
            }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }
}