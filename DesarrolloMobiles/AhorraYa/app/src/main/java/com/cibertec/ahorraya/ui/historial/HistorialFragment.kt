package com.cibertec.ahorraya.ui.historial

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.ahorraya.R
import com.cibertec.ahorraya.databinding.FragmentHistorialBinding
import com.cibertec.ahorraya.repository.Movimiento
import com.cibertec.ahorraya.ui.adapter.MovAdapter
import com.cibertec.ahorraya.ui.main.MainActivity
import kotlinx.coroutines.*

/**
 * HistorialFragment
 * - Lista y busca movimientos desde SQLite
 * - Elimina con menú contextual (long-press)
 */
class HistorialFragment : Fragment() {

    private var _b: FragmentHistorialBinding? = null
    private val b get() = _b!!

    private val repo by lazy { (requireActivity() as MainActivity).repo }
    private val ui = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var adapter: MovAdapter
    private var ultimaBusqueda: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentHistorialBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prepara RecyclerView
        adapter = MovAdapter { item -> mostrarMenuItem(item) }
        b.rvLista.layoutManager = LinearLayoutManager(requireContext())
        b.rvLista.adapter = adapter

        // Carga inicial
        cargarLista()

        // Búsqueda en tiempo real
        b.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(e: Editable?) {
                ultimaBusqueda = e?.toString()?.trim().orEmpty()
                cargarLista(ultimaBusqueda)
            }
        })
    }

    // Carga la lista (completa o filtrada) desde SQLite en background
    private fun cargarLista(q: String? = null) {
        ui.launch(Dispatchers.IO) {
            val lista = repo.listar(q)
            withContext(Dispatchers.Main) {
                b.txtVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                adapter.submit(lista)
            }
        }
    }

    // Muestra menú contextual para eliminar
    private fun mostrarMenuItem(item: Movimiento) {
        val pm = PopupMenu(requireContext(), b.rvLista)
        pm.menu.add(0, 1, 0, getString(R.string.menu_eliminar))
        pm.setOnMenuItemClickListener {
            if (it.itemId == 1) {
                ui.launch(Dispatchers.IO) {
                    repo.eliminar(item.id)
                    cargarLista(ultimaBusqueda) // refresca
                }
            }
            true
        }
        pm.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.cancel()
        _b = null
    }
}