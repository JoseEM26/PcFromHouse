package com.cibertec.ahorraya.UI.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.ahorraya.R
import com.cibertec.ahorraya.Repository.Movimiento
import com.cibertec.ahorraya.databinding.ItemMovimientoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovAdapter (
    private val onLongClick :(Movimiento) -> Unit): RecyclerView.Adapter<MovAdapter.VH>() {

        private val items= mutableListOf<Movimiento>()
        private val df=SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("es","PE"))


    fun submit(list:List<Movimiento>){
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
    inner class VH(val b:ItemMovimientoBinding):RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): VH {
        return VH(ItemMovimientoBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int =items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item=items[pos]

        h.b.txtTipoMonto.text="${item.tipo}- " +
                h.b.root.context.getString(R.string.fmt_monedas ,item.monto)

        val fechatxt=df.format(Date(item.fechaMillis))
        h.b.txtDescFecha.text="${item.descripcion ?:"-"}-$fechatxt"

        h.b.root.contentDescription= "Movimiento ${item.tipo} , monto "+ h.b.root.context.getString(R.string.fmt_monedas , item.monto)+
                ",fecha $fechatxt"

        h.b.root.setOnLongClickListener{ _ -> onLongClick(item) ; true}

    }


}