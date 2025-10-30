package com.example.gestorperfils

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuPrincipalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        val btnAnarALlista = findViewById<Button>(R.id.btnAnarALlista)

        btnAnarALlista.setOnClickListener {
            // Navegació a la teva Activity de llista de perfils
            val intent = Intent(this, LlistaPerfilsActivity::class.java)
            startActivity(intent)
        }

        // Aquí afegiries més botons per a altres funcionalitats futures (Ajustos, Nou Perfil, etc.)
    }
}