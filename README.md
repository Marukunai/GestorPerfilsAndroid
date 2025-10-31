# 📱 Gestor de Perfils (Android Kotlin)

## ✨ Descripció del Projecte

Aquesta aplicació Android, desenvolupada íntegrament en **Kotlin**, és un gestor complet de perfils d'usuari. Permet la gestió persistent de dades, la creació, edició i eliminació de perfils, i ofereix funcionalitats avançades per a una experiència d'usuari (UX) òptima.

Les dades es persisteixen localment mitjançant fitxers **JSON** a l'emmagatzematge intern del dispositiu, assegurant que els canvis es mantinguin entre sessions.

---

## 🚀 Funcionalitats Principals

* **Llista de Perfils:** Visualització en llista amb `RecyclerView`.
* **Detall i Edició:** Pantalla per visualitzar i modificar els camps de cada perfil.
* **Persistència JSON:** Les dades es guarden i es carreguen utilitzant `Gson` per mantenir l'estat.
* **Assignació d'ID Única:** Generació automàtica d'ID per a cada nou perfil.
* **Cerca Dinàmica:** Filtració en temps real de la llista utilitzant `SearchView`.
* **Eliminació Segura:**
    * Eliminació amb confirmació des de la pantalla de detall.
    * ***Swipe-to-Delete*** amb confirmació per a una UX fluida.
* **Pantalla d'Estadístiques:** Mostra mètriques clau (total de perfils, actius/inactius i mitjana d'edat).
* **Manteniment de Dades:** Opció al menú principal per **Restaurar Dades** (reset) al seu estat inicial des del fitxer d'Assets.

---

## 🛠️ Tecnologies i Llibreries

* **Llenguatge de Programació:** Kotlin
* **Arquitectura:** Activities (Amb ús de `registerForActivityResult`)
* **Persistència:** JSON + Emmagatzematge Intern d'Android (`Internal Storage`)
* **Libreries:**
    * `Gson` per la serialització/deserialització JSON.
    * `AndroidX AppCompat` i `ConstraintLayout`.
    * `RecyclerView` i `ItemTouchHelper` per la llista i el *swipe-to-delete*.
    * `AlertDialog` per les confirmacions de seguretat.

---

## ⚙️ Estructura de Dades

El projecte utilitza el `data class` `PerfilUsuari` per a la gestió de la informació. La persistència es basa en el fitxer `dades_usuaris.json`.

```kotlin
data class PerfilUsuari(
    val id: Int,
    val nom: String,
    val cognom: String,
    val edat: Int,
    val email: String,
    val imatgeUrl: String,
    val actiu: Boolean
) : Parcelable
```

---

## 🖼️ Screenshots

En aquest apartat veurem les diferents pantalles de l'app, des de la pantalla d'inici fins a la d'estadístiques, per ordre.
