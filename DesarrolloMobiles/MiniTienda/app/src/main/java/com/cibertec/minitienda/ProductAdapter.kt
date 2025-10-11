package com.cibertec.minitienda

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.minitienda.databinding.ItemProductoBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

// Adaptador para el RecyclerView que maneja los productos
class ProductAdapter(private val items: MutableList<Product>, private val onListChanged: () -> Unit
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    // ViewHolder: Se encarga de enlazar cada elemento del producto a la UI
    inner class VH(val binding: ItemProductoBinding) : RecyclerView.ViewHolder(binding.root)

    // Crea una nueva vista para un item de producto
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // Inflar el layout del item de producto
        val inf = LayoutInflater.from(parent.context)
        val binding = ItemProductoBinding.inflate(inf, parent, false)
        return VH(binding)
    }

    // Devuelve el tamaño de la lista de productos
    override fun getItemCount(): Int = items.size

    // Asocia los datos del producto con las vistas en cada item del RecyclerView
    override fun onBindViewHolder(holder: VH, position: Int) {
        val ctx = holder.binding.root.context  // Contexto de la aplicación
        val p = items[position]  // Producto actual
        val b = holder.binding  // Binding del item del RecyclerView

        // Establecer la imagen del producto (si existe)
        if (p.imageUri != null) {
            b.imgThumb.setImageURI(p.imageUri)  // Mostrar imagen seleccionada
        } else {
            b.imgThumb.setImageResource(R.mipmap.ic_launcher)  // Mostrar imagen por defecto
        }

        // Establecer el nombre del producto
        b.txtNombre.text = p.nombre

        // Crear una cadena con el detalle del producto (disponibilidad, categoría y estado)
        val disponibleTexto = if (p.disponible) "Disponible" else "No disponible"
        val derecha = "${p.categoria} • ${p.estado}"
        b.txtDetalle.text = ctx.getString(R.string.fmt_detalle, disponibleTexto, derecha)

        // Establecer el precio del producto con formato de moneda
        b.txtPrecio.text = ctx.getString(R.string.fmt_precio, ctx.getString(R.string.moneda_prefijo), money(p.precio))

        // Menú contextual cuando el usuario hace un long-press en el item
        b.root.setOnLongClickListener {
            val pop = PopupMenu(ctx, b.root)  // Crear un menú desplegable
            pop.menu.add(ctx.getString(R.string.menu_editar_cantidad))  // Opción para editar cantidad
            pop.menu.add(ctx.getString(R.string.menu_eliminar))  // Opción para eliminar el producto
            pop.setOnMenuItemClickListener { item ->
                when (item.title) {
                    ctx.getString(R.string.menu_editar_cantidad) -> {
                        // Mostrar diálogo para editar la cantidad del producto
                        showEditCantidadDialog(ctx, p) {
                            notifyItemChanged(position)  // Notificar que el item cambió
                            onListChanged()  // Actualizar el resumen de totales
                        }
                        true
                    }
                    ctx.getString(R.string.menu_eliminar) -> {
                        // Eliminar el producto de la lista
                        val idx = holder.bindingAdapterPosition
                        if (idx != RecyclerView.NO_POSITION) {
                            items.removeAt(idx)
                            notifyItemRemoved(idx)  // Notificar que el item fue eliminado
                            onListChanged()  // Actualizar el resumen de totales
                        }
                        true
                    }
                    else -> false
                }
            }
            pop.show()  // Mostrar el menú contextual
            true
        }
    }

    // Método para mostrar un cuadro de diálogo que permite editar la cantidad de un producto
    private fun showEditCantidadDialog(ctx: android.content.Context, p: Product, onDone: () -> Unit) {
        val input = EditText(ctx).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER  // Solo números
            setText(p.cantidad.toString())  // Establecer la cantidad actual
            setSelection(text.length)  // Colocar el cursor al final del texto
        }

        // Crear un diálogo de alerta para editar la cantidad
        AlertDialog.Builder(ctx)
            .setTitle(ctx.getString(R.string.menu_editar_cantidad))
            .setView(input)  // Establecer el campo de texto como vista
            .setPositiveButton(android.R.string.ok) { d, _ ->
                // Validar la cantidad ingresada
                val n = input.text.toString().trim().toIntOrNull()
                if (n == null || n <= 0) {
                    input.error = ctx.getString(R.string.error_cantidad_requerida)
                } else if (n > 100) {
                    input.error = ctx.getString(R.string.error_cantidad_max)
                } else {
                    p.cantidad = n  // Actualizar la cantidad del producto
                    onDone()  // Llamar al callback para actualizar la lista
                    d.dismiss()  // Cerrar el diálogo
                }
            }
            .setNegativeButton(android.R.string.cancel, null)  // Botón para cancelar
            .show()  // Mostrar el diálogo
    }

    // Método para agregar un producto a la lista
    fun add(product: Product) {
        items.add(product)  // Agregar el producto al final de la lista
        notifyItemInserted(items.lastIndex)  // Notificar que un nuevo item ha sido agregado
        onListChanged()  // Actualizar el resumen de totales
    }

    // Función para formatear el precio como moneda (en formato local de Perú)
    private fun money(n: Double): String {
        val loc = Locale("es", "PE")  // Configuración regional de Perú
        return String.format(loc, "%,.2f", n)  // Formatear el precio como moneda
    }
}