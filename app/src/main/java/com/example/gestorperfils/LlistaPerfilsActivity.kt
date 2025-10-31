package com.example.gestorperfils

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import android.content.Intent
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import java.io.File
import java.io.FileWriter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog // 💡 NOU IMPORT: Per al Diàleg de Confirmació

class LlistaPerfilsActivity : AppCompatActivity() {

    // Variables per a la interfície
    private lateinit var rvPerfils: RecyclerView
    private val FITXER_DADES = "dades_usuaris.json" // El nom del fitxer de persistència

    // Aquí emmagatzemarem la llista d'usuaris
    private var llistaPerfils: List<PerfilUsuari> = emptyList()

    // CLAU NECESSÀRIA: Cal que la clau del Perfil Modificat estigui disponible per al Launcher
    companion object {
        const val EXTRA_PERFIL_MODIFICAT_KEY = "com.example.gestorperfils.PERFIL_MODIFICAT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llista_perfils)

        // 1. Inicialitzar el RecyclerView (trobar la vista)
        rvPerfils = findViewById(R.id.rvPerfils)

        // 2. Carregar les dades des del JSON (amb persistència)
        llistaPerfils = carregarDades()

        Log.d("JSON_LOAD", "Perfils carregats: ${llistaPerfils.size}")

        // 3. Configurar i mostrar el RecyclerView
        configurarRecyclerView()

        // 4. Configurar el Swipe-to-Delete (NOU)
        configurarSwipeToDelete()

        // 5. Configurar la Toolbar i el Menu
        configurarToolbar()
    }

// -----------------------------------------------------------------
// Lògica de Refresc de la Llista
// -----------------------------------------------------------------

    /**
     * Recarrega les dades des de l'emmagatzematge intern i reinicialitza l'Adapter.
     * Això garanteix que els canvis es mostrin immediatament després de tornar d'una Activity.
     */
    private fun refrescarLlistaCompletament() {
        // 1. Recarrega la llista de perfils des de l'emmagatzematge intern
        llistaPerfils = carregarDades()

        // 2. Reinicialitza l'Adapter (si rvPerfils ja està inicialitzat)
        if (::rvPerfils.isInitialized) {
            configurarRecyclerView()
        }
    }

// -----------------------------------------------------------------
// Lògica del JSON i Persistència
// -----------------------------------------------------------------

    /**
     * Guarda la llista actual de perfils com a JSON a l'emmagatzematge intern.
     */
    private fun guardarLlistaAInternalStorage() {
        try {
            val jsonString = Gson().toJson(llistaPerfils)

            // openFileOutput és un mètode d'Activity per obrir fitxers interns
            openFileOutput(FITXER_DADES, MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
            Log.d("JSON_SAVE", "Dades guardades correctament a $FITXER_DADES")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("JSON_SAVE", "Error guardant dades: ${e.message}")
        }
    }

    /**
     * Carrega el JSON: primer des de Internal Storage, si no existeix el fitxer, des dels Assets.
     */
    private fun carregarDades(): List<PerfilUsuari> {
        val file = File(filesDir, FITXER_DADES)
        val listType = object : TypeToken<List<PerfilUsuari>>() {}.type

        val jsonString: String? = try {
            if (file.exists()) {
                // Opció 1: Carregar des de Internal Storage (conté els canvis)
                Log.d("JSON_LOAD", "Carregant des de Internal Storage.")
                file.bufferedReader().use { it.readText() }
            } else {
                // Opció 2: Carregar des dels Assets (dades inicials si és la primera execució)
                Log.d("JSON_LOAD", "Carregant des d'Assets (dades inicials).")
                assets.open("dades_usuaris.json").bufferedReader().use { it.readText() }
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyList()
        }

        return jsonString?.let { Gson().fromJson<List<PerfilUsuari>>(it, listType) } ?: emptyList()
    }

// -----------------------------------------------------------------
// Activity Result Launchers
// -----------------------------------------------------------------

    /**
     * Launcher per rebre resultats de NouPerfilActivity (Afegir).
     */
    private val nouPerfilLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val nouPerfilSenseId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(NouPerfilActivity.EXTRA_NOU_PERFIL, PerfilUsuari::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(NouPerfilActivity.EXTRA_NOU_PERFIL)
            }

            nouPerfilSenseId?.let { perfil ->
                // 💡 PAS D'ASSIGNACIÓ D'ID ÚNICA
                val novaId = obtenirProximaId()
                val nouPerfilAmbId = perfil.copy(id = novaId)

                afegirPerfil(nouPerfilAmbId) // Afegeix i guarda el perfil AMB ID
                refrescarLlistaCompletament() // Força l'actualització de la vista
            }
        }
    }

    /**
     * Launcher per rebre resultats de DetallPerfilActivity (Eliminar/Modificar).
     */
    private val detallPerfilLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            DetallPerfilActivity.RESULT_ELIMINAR -> { // Codi de resultat per a l'eliminació
                val perfilIdAEliminar = result.data?.getIntExtra(DetallPerfilActivity.EXTRA_PERFIL_ID_ELIMINAR, -1)

                if (perfilIdAEliminar != -1) {
                    eliminarPerfil(perfilIdAEliminar!!) // Elimina i guarda
                }
                refrescarLlistaCompletament() // Força l'actualització de la vista
            }
            RESULT_OK -> { // Codi de resultat per a la MODIFICACIÓ
                val perfilModificat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(EXTRA_PERFIL_MODIFICAT_KEY, PerfilUsuari::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(EXTRA_PERFIL_MODIFICAT_KEY)
                }

                perfilModificat?.let {
                    actualitzarPerfil(it) // Actualitza i guarda
                }
                refrescarLlistaCompletament() // Força l'actualització
            }
        }
    }

// -----------------------------------------------------------------
// Lògica de Gestió de Llista (Update, Create, Delete)
// -----------------------------------------------------------------

    /**
     * Funció per actualitzar un perfil existent a la llista.
     */
    private fun actualitzarPerfil(perfilModificat: PerfilUsuari) {
        val index = llistaPerfils.indexOfFirst { it.id == perfilModificat.id }

        if (index != -1) {
            val llistaMutable = llistaPerfils.toMutableList()
            llistaMutable[index] = perfilModificat // Substitueix l'objecte antic
            llistaPerfils = llistaMutable.toList()

            // 💾 GUARDA ELS CANVIS
            guardarLlistaAInternalStorage()
        }
    }

    private fun afegirPerfil(nouPerfil: PerfilUsuari) {
        val llistaMutable = llistaPerfils.toMutableList()
        llistaMutable.add(nouPerfil)
        llistaPerfils = llistaMutable.toList()

        // 💾 GUARDA ELS CANVIS
        guardarLlistaAInternalStorage()
    }

    /**
     * ELIMINA UN PERFIL PER ID i guarda els canvis.
     */
    private fun eliminarPerfil(id: Int) {
        val index = llistaPerfils.indexOfFirst { it.id == id }

        if (index != -1) {
            val llistaMutable = llistaPerfils.toMutableList()
            llistaMutable.removeAt(index)
            llistaPerfils = llistaMutable.toList() // Actualitza la llista

            // 💾 GUARDA ELS CANVIS
            guardarLlistaAInternalStorage()

            // La crida a refrescarLlistaCompletament() ja es fa des dels launchers
        }
    }

    /**
     * ELIMINA UN PERFIL PER POSICIÓ de l'Adapter (usat pel Swipe-to-Delete).
     */
    private fun eliminarPerfilEnPosicio(position: Int) {
        // Obtenir l'ID del perfil a eliminar de la llista que mostra l'Adapter
        val adapter = rvPerfils.adapter as? PerfilsAdapter

        // Només podem eliminar elements de la llista original, no de la filtrada temporal
        val perfilAEliminar = adapter?.getPerfilAt(position)

        if (perfilAEliminar != null) {
            // 1. Elimina de la llista persistent (llistaPerfils)
            val idAEliminar = perfilAEliminar.id
            val indexOriginal = llistaPerfils.indexOfFirst { it.id == idAEliminar }

            if (indexOriginal != -1) {
                val llistaMutable = llistaPerfils.toMutableList()
                llistaMutable.removeAt(indexOriginal)
                llistaPerfils = llistaMutable.toList()

                // 2. Guarda els canvis a Internal Storage
                guardarLlistaAInternalStorage()

                // 3. Notifica l'Adapter (l'Adapter ja té la seva pròpia lògica de notificació)
                adapter.notifyItemRemoved(position)

                // 4. Forcem el refresc de l'Adapter amb les dades actualitzades (i sense filtre)
                refrescarLlistaCompletament()
            }
        }
    }

    /**
     * Calcula i retorna les estadístiques bàsiques de la llista de perfils.
     */
    private fun calcularEstadistiques(): Map<String, Any> {
        val total = llistaPerfils.size
        val actius = llistaPerfils.count { it.actiu }
        val inactius = total - actius

        // Calcular la mitjana d'edat. Si la llista és buida, retorna 0.0
        val mitjanaEdat = if (total > 0) {
            llistaPerfils.map { it.edat.toDouble() }.average()
        } else {
            0.0
        }

        return mapOf(
            "total" to total,
            "actius" to actius,
            "inactius" to inactius,
            "mitjanaEdat" to mitjanaEdat
        )
    }

    /**
     * Calcula la pròxima ID única trobant la ID màxima existent i sumant-li 1.
     * Si la llista és buida, retorna 1.
     */
    private fun obtenirProximaId(): Int {
        if (llistaPerfils.isEmpty()) {
            return 1
        }
        // Troba l'ID màxima de la llista i suma 1
        val maxId = llistaPerfils.maxOf { it.id }
        return maxId + 1
    }

    // -----------------------------------------------------------------
    // LÒGICA DEL DIÀLEG DE SWIPE (NOVA)
    // -----------------------------------------------------------------

    /**
     * Mostra un AlertDialog per confirmar l'eliminació per swipe.
     */
    private fun mostrarDialegConfirmacioSwipe(position: Int) {
        val adapter = rvPerfils.adapter as? PerfilsAdapter
        val perfilAEliminar = adapter?.getPerfilAt(position)

        if (perfilAEliminar == null) {
            adapter?.notifyItemChanged(position) // Reseteja la vista si no hi ha perfil
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminació")
            .setMessage("Estàs segur que vols eliminar el perfil de ${perfilAEliminar.nom} ${perfilAEliminar.cognom}?")
            .setPositiveButton("Eliminar") { dialog, which ->
                // Si l'usuari confirma, executa la funció d'eliminació real
                eliminarPerfilEnPosicio(position)
            }
            .setNegativeButton("Cancel·lar") { dialog, which ->
                // Si l'usuari cancel·la, desfa l'acció de swipe (torna l'element)
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    // -----------------------------------------------------------------
// Lògica de la Toolbar/Menú (AMB LÒGICA DE CERCA)
// -----------------------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 1. Infla el menú (que ara conté l'ítem 'action_cercar')
        menuInflater.inflate(R.menu.menu_llista, menu)

        // 2. Troba l'ítem de cerca i assigna el SearchView
        val searchItem = menu?.findItem(R.id.action_cercar)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Cercar per nom o cognom..."

        // 3. Configura el listener de cerca
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Quan l'usuari prem ENTRAR
                (rvPerfils.adapter as? PerfilsAdapter)?.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Quan l'usuari escriu cada caràcter
                (rvPerfils.adapter as? PerfilsAdapter)?.filter(newText.orEmpty())
                return true
            }
        })

        // 4. Resetegem la llista quan es tanca la cerca (opcional, però bona pràctica)
        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Quan la cerca s'expandeix (no fem res)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Quan la cerca es col·lapsa (cercar per buit = mostrar tots)
                (rvPerfils.adapter as? PerfilsAdapter)?.filter("")
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_afegir_perfil -> {
                val intent = Intent(this, NouPerfilActivity::class.java)
                nouPerfilLauncher.launch(intent) // Llança per afegir perfil
                true
            }
            R.id.action_estadistiques -> { // 💡 NOVA ACCIÓ
                val stats = calcularEstadistiques()
                val intent = Intent(this, EstadistiquesActivity::class.java).apply {
                    putExtra(EstadistiquesActivity.EXTRA_TOTAL_PERFILS, stats["total"] as Int)
                    putExtra(EstadistiquesActivity.EXTRA_ACTIUS_PERFILS, stats["actius"] as Int)
                    putExtra(EstadistiquesActivity.EXTRA_INACTIUS_PERFILS, stats["inactius"] as Int)
                    putExtra(EstadistiquesActivity.EXTRA_MITJANA_EDAT, stats["mitjanaEdat"] as Double)
                }
                startActivity(intent)
                true
            }
            // La cerca es gestiona al onCreateOptionsMenu, així que la deixem fora d'aquí.
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Configura la Toolbar i infla el menú.
     */
    private fun configurarToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarLlista)
        setSupportActionBar(toolbar)
    }

// -----------------------------------------------------------------
// Lògica del RecyclerView i Navegació
// -----------------------------------------------------------------

    /**
     * Configura el LayoutManager i l'Adapter del RecyclerView.
     */
    private fun configurarRecyclerView() {
        rvPerfils.layoutManager = LinearLayoutManager(this)

        // Assegurem que l'Adapter es crea amb la llista completa (la llista base per filtrar)
        rvPerfils.adapter = PerfilsAdapter(llistaPerfils) { perfilSeleccionat ->
            // Acció en clicar un element: iniciar la Activity 2 (Detall)
            navegarADetall(perfilSeleccionat)
        }
    }

    /**
     * Configura el comportament de lliscament per eliminar. (MODIFICAT PER AL DIÀLEG)
     */
    private fun configurarSwipeToDelete() {
        // Definim la funció de callback
        val itemTouchHelperCallback = object : SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No permetem moure elements (drag and drop)
            }

            // Aquest mètode es crida quan un element es llisca
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // 💡 CANVI CLAU: Cridem el diàleg de confirmació en lloc d'eliminar directament
                    mostrarDialegConfirmacioSwipe(position)
                }
            }

            // Opcional: Afegir el fons vermell i la icona de paperera
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                val icon = ContextCompat.getDrawable(this@LlistaPerfilsActivity, android.R.drawable.ic_menu_delete)
                val iconMargin = (itemView.height - (icon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + (itemView.height - (icon?.intrinsicHeight ?: 0)) / 2
                val iconBottom = iconTop + (icon?.intrinsicHeight ?: 0)

                if (dX > 0) { // Swiping cap a la dreta (Right)
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + (icon?.intrinsicWidth ?: 0)
                    icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                } else if (dX < 0) { // Swiping cap a l'esquerra (Left)
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    val iconLeft = itemView.right - iconMargin - (icon?.intrinsicWidth ?: 0)
                    val iconRight = itemView.right - iconMargin
                    icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                } else { // Sense swiping
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)
                icon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        // Connectar el callback al RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvPerfils)
    }

    /**
     * Funció per iniciar l'Activity de detall (ara utilitza el Launcher)
     */
    private fun navegarADetall(perfil: PerfilUsuari) {
        val intent = Intent(this, DetallPerfilActivity::class.java).apply {
            putExtra(DetallPerfilActivity.PERFIL_SELECCIONAT_KEY, perfil)
        }
        // Utilitzem el launcher per esperar el resultat (eliminació o modificació)
        detallPerfilLauncher.launch(intent)
    }
}