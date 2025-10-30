package com.example.gestorperfils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter per al RecyclerView que mostra la llista de Perfils, ara amb funcionalitat de cerca i eliminació per lliscament.
 * @param perfils La llista de dades PerfilUsuari a mostrar inicialment.
 * @param onClickListener Una funció lambda que s'executarà quan es faci click a un ítem.
 */
class PerfilsAdapter(
    private val llistaOriginal: List<PerfilUsuari>, // La llista completa, inalterada
    private val onClickListener: (PerfilUsuari) -> Unit
) : RecyclerView.Adapter<PerfilsAdapter.PerfilViewHolder>(), Filterable {

    // La llista que el RecyclerView utilitzarà per mostrar les dades
    private var llistaFiltrada: List<PerfilUsuari> = llistaOriginal

    // -----------------------------------------------------------
    // 1. ViewHolder: Conté les vistes (UI elements) de l'ítem
    // -----------------------------------------------------------
    inner class PerfilViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNomComplet: TextView = view.findViewById(R.id.tvNomComplet)
        val tvEdat: TextView = view.findViewById(R.id.tvEdat)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)

        // Funció per assignar les dades i el click listener
        fun bind(perfil: PerfilUsuari) {
            tvNomComplet.text = "${perfil.nom} ${perfil.cognom}"
            tvEdat.text = "Edat: ${perfil.edat} anys"

            // La imatge es deixa com a placeholder per simplicitat
            // Aquí normalment faries: ivAvatar.setImageResource(R.drawable.un_recurs_drawable)

            // Gestionar el click a tot l'ítem
            itemView.setOnClickListener {
                onClickListener(perfil)
            }
        }
    }

    // -----------------------------------------------------------
    // 2. onCreateViewHolder: Infla el disseny de l'ítem (XML -> View)
    // -----------------------------------------------------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerfilViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_perfil, parent, false)
        return PerfilViewHolder(view)
    }

    // -----------------------------------------------------------
    // 3. onBindViewHolder: Omple les dades de cada ítem
    // -----------------------------------------------------------
    override fun onBindViewHolder(holder: PerfilViewHolder, position: Int) {
        // IMPORTANT: Utilitzem la llista filtrada
        val perfil = llistaFiltrada[position]
        holder.bind(perfil)
    }

    // -----------------------------------------------------------
    // 4. getItemCount: Retorna el nombre total d'ítems
    // -----------------------------------------------------------
    // IMPORTANT: Retorna la mida de la llista filtrada
    override fun getItemCount() = llistaFiltrada.size

    // -----------------------------------------------------------
    // 5. Mètode de Filtratge (Requerit per Filterable)
    // -----------------------------------------------------------

    /**
     * Retorna l'objecte Filter que realitza l'operació de filtratge
     * en funció del text de la cerca.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            // Aquest mètode s'executa en un fil secundari
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString().toLowerCase()
                val results = FilterResults()

                if (charSearch.isEmpty()) {
                    // Si el text de cerca és buit, mostrem la llista completa
                    results.values = llistaOriginal
                } else {
                    // Filtrem per nom o cognom
                    val resultList = llistaOriginal.filter {
                        it.nom.toLowerCase().contains(charSearch) ||
                                it.cognom.toLowerCase().contains(charSearch)
                    }
                    results.values = resultList
                }
                return results
            }

            // Aquest mètode s'executa al fil principal (UI)
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                // Actualitzem la llista i notifiquem els canvis
                @Suppress("UNCHECKED_CAST")
                llistaFiltrada = results?.values as? List<PerfilUsuari> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    // -----------------------------------------------------------
    // 6. Funció Pública de Filtratge (Cridada des de l'Activity)
    // -----------------------------------------------------------

    /**
     * Mètode cridat des de LlistaPerfilsActivity per iniciar el filtratge.
     */
    fun filter(query: String) {
        getFilter().filter(query)
    }

    // -----------------------------------------------------------
    // 7. Funció d'Accés al Perfil (Usada pel Swipe-to-Delete)
    // -----------------------------------------------------------

    /**
     * Retorna el PerfilUsuari que es troba a la posició actual de la llista filtrada.
     * Crucial per al Swipe-to-Delete, ja que la posició de l'Adapter
     * pot no coincidir amb la posició de la llista original.
     */
    fun getPerfilAt(position: Int): PerfilUsuari {
        return llistaFiltrada[position]
    }
}