package com.example.gestorperfils

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EstadistiquesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TOTAL_PERFILS = "total_perfils"
        const val EXTRA_ACTIUS_PERFILS = "actius_perfils"
        const val EXTRA_INACTIUS_PERFILS = "inactius_perfils"
        const val EXTRA_MITJANA_EDAT = "mitjana_edat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 💡 Assumeix que tens un layout anomenat activity_estadistiques
        setContentView(R.layout.activity_estadistiques)

        // Configurar la Toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Estadístiques dels Perfils"
        }

        // 1. Recuperar dades de l'Intent
        val total = intent.getIntExtra(EXTRA_TOTAL_PERFILS, 0)
        val actius = intent.getIntExtra(EXTRA_ACTIUS_PERFILS, 0)
        val inactius = intent.getIntExtra(EXTRA_INACTIUS_PERFILS, 0)
        val mitjanaEdat = intent.getDoubleExtra(EXTRA_MITJANA_EDAT, 0.0)

        // 2. Trobar vistes (Assumeix que els IDs són correctes al teu layout)
        val tvTotal = findViewById<TextView>(R.id.tvTotalPerfiles)
        val tvActius = findViewById<TextView>(R.id.tvActius)
        val tvInactius = findViewById<TextView>(R.id.tvInactius)
        val tvMitjanaEdat = findViewById<TextView>(R.id.tvMitjanaEdat)

        // 3. Mostrar dades
        tvTotal.text = "Total de perfils: $total"
        tvActius.text = "Perfils Actius: $actius (${String.format("%.1f", (actius.toDouble()/total) * 100)}%)"
        tvInactius.text = "Perfils Inactius: $inactius (${String.format("%.1f", (inactius.toDouble()/total) * 100)}%)"
        tvMitjanaEdat.text = "Mitjana d'Edat: ${String.format("%.1f", mitjanaEdat)} anys"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}