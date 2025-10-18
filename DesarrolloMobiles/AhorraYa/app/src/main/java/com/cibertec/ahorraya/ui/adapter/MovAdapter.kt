package com.cibertec.ahorraya.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ahorraya.R
import com.cibertec.ahorraya.databinding.ItemMovimientoBinding
import com.cibertec.ahorraya.repository.Movimiento
import java.text.SimpleDateFormat
import java.util.*

/**
 * MovAdapter
 * - Renderiza cada movimiento en una tarjeta Material
 * - Expone onLongClick para acciones (eliminar)
 */
class MovAdapter(
    private val onLongClick: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovAdapter.VH>() {

    private val items = mutableListOf<Movimiento>()
    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es","PE"))

    fun submit(list: List<Movimiento>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemMovimientoBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMovimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]

        // Línea superior: tipo • monto
        h.b.txtTipoMonto.text = "${item.tipo} • " +
            h.b.root.context.getString(R.string.fmt_monedas, item.monto)

        // Línea inferior: descripcion • fecha
        val fechaTxt = df.format(Date(item.fechaMillis))
        h.b.txtDescFecha.text = "${item.descripcion ?: "—"} • $fechaTxt"

        // Descripción para TalkBack/VoiceOver
        h.b.root.contentDescription =
            "Movimiento ${item.tipo}, monto " +
            h.b.root.context.getString(R.string.fmt_monedas, item.monto) +
            ", fecha $fechaTxt"

        // Acción de mantener presionado
        h.b.root.setOnLongClickListener { _ -> onLongClick(item); true }
    }

    override fun getItemCount(): Int = items.size
}