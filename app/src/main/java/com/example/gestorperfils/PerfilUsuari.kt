package com.example.gestorperfils

// PerfilUsuari.kt

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize // <-- PROVA AMB AQUESTA!

@Parcelize
data class PerfilUsuari(
    val id: Int,
    val nom: String,
    val cognom: String,
    val edat: Int,
    val email: String,
    val imatge_url: String,
    val actiu: Boolean
) : Parcelable