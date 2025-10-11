package com.cibertec.minitienda

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.minitienda.databinding.ActivityMainBinding
import com.cibertec.minitienda.databinding.DialogFormProductoBinding
import java.util.Locale

// Clase principal de la actividad
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;
    private val productos= mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    private var dialogBinding:DialogFormProductoBinding?=null
    private var pickedImagenUri:Uri?=null

    private val pickImage=registerForActivityResult(ActivityResultContracts.GetContent()){
        uri-> if(uri!=null){
            pickedImagenUri=uri
            dialogBinding?.imgPreview?.setImageURI(uri)
    }
    }

    private var nextId=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ArrayAdapter.createFromResource(this,R.array.estado_items,android.R.layout.simple_spinner_item).also {
            ad-> ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spEstado.adapter=ad
        }
        adapter = ProductAdapter(productos) { updateResumen() }
        binding.rvProductos.layoutManager = LinearLayoutManager(this)  // Disposición en lista vertical
        binding.rvProductos.adapter = adapter  // Asignación del adaptador al RecyclerView

        // Configuración del botón "Agregar", abre un diálogo para ingresar un nuevo producto
        binding.btnAgregar.setOnClickListener {
            openNuevoProductoDialog()
        }

        // Mostrar el resumen inicial de los productos
        updateResumen()


    }
    private fun openNuevoProductoDialog() {
        // Inflar el layout del diálogo
        val b = DialogFormProductoBinding.inflate(layoutInflater)
        dialogBinding = b
        pickedImagenUri = null  // Reiniciar la URI de la imagen seleccionada

        // Configuración de la lista de categorías usando un ListView
        val cats = resources.getStringArray(R.array.categorias_items)
        b.lvCategorias.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, cats)
        b.lvCategorias.choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE  // Selección de un solo ítem
        b.lvCategorias.setItemChecked(0, true) // Preseleccionar la primera categoría

        // Configurar el comportamiento para seleccionar una imagen desde la galería
        b.imgPreview.setOnClickListener { pickImage.launch("image/*") }

        // Crear el diálogo de alerta con los botones de guardar y cancelar
        val dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_titulo_form))
            .setView(b.root)  // Establecer el layout del diálogo
            .setPositiveButton(getString(R.string.btn_guardar), null) // Guardar se maneja manualmente
            .setNegativeButton(getString(R.string.btn_cancelar)) { d, _ ->
                // Cerrar el diálogo sin guardar
                dialogBinding = null
                pickedImagenUri = null
                d.dismiss()
            }
            .create()

        // Cuando el diálogo se muestre, se configura el botón de guardar
        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                // Validaciones de los campos del formulario
                val nombre = b.edtNombre.text?.toString()?.trim().orEmpty()
                val precio = b.edtPrecio.text?.toString()?.trim()?.replace(",", ".")?.toDoubleOrNull()
                val cantidad = b.edtCantidad.text?.toString()?.trim()?.toIntOrNull()

                var ok = true
                // Limpiar errores anteriores
                b.tilNombre.error = null
                b.tilPrecio.error = null
                b.tilCantidad.error = null

                // Validación de los campos
                if (nombre.isEmpty()) {
                    b.tilNombre.error = getString(R.string.error_nombre_requerido)
                    ok = false
                }
                if (precio == null || precio <= 0.0) {
                    b.tilPrecio.error = getString(R.string.error_precio_requerido)
                    ok = false
                }
                if (cantidad == null || cantidad <= 0) {
                    b.tilCantidad.error = getString(R.string.error_cantidad_requerida)
                    ok = false
                } else if (cantidad > 100) {
                    b.tilCantidad.error = getString(R.string.error_cantidad_max)
                    ok = false
                }

                // Si las validaciones son correctas, proceder a agregar el producto
                if (!ok) return@setOnClickListener

                // Obtener el estado desde el Spinner principal
                val estado = binding.spEstado.selectedItem?.toString().orEmpty()

                // Obtener la categoría seleccionada desde el ListView
                val posCat = b.lvCategorias.checkedItemPosition.takeIf { it >= 0 } ?: 0
                val categoria = cats[posCat]

                // Obtener si el producto está disponible o no
                val disponible = b.switchDisponible.isChecked

                // Crear un nuevo objeto de producto con los datos ingresados
                val p = Product(
                    id = nextId++,  // Asignar un ID único
                    nombre = nombre,
                    precio = precio!!,
                    cantidad = cantidad!!,
                    estado = estado,
                    categoria = categoria,
                    disponible = disponible,
                    imageUri = pickedImagenUri  // Imagen seleccionada
                )
                // Agregar el producto a la lista y actualizar la vista
                adapter.add(p)

                // Limpiar los campos y cerrar el diálogo
                dialogBinding = null
                pickedImagenUri = null
                dlg.dismiss()
            }
        }

        dlg.show()  // Mostrar el diálogo
    }
    private fun updateResumen() {
        val subtotal = productos.sumOf { it.precio * it.cantidad }
        val igv = subtotal * 0.18
        val total = subtotal + igv

        // Mostrar el resumen en el TextView correspondiente
        val pref = getString(R.string.moneda_prefijo)
        binding.txtResumen.text = buildString {
            appendLine(getString(R.string.lbl_subtotal, pref, money(subtotal)))
            appendLine(getString(R.string.lbl_igv, pref, money(igv)))
            append(getString(R.string.lbl_total, pref, money(total)))
        }
    }
    private fun money(n: Double): String {
        val loc = Locale("es", "PE")  // Configuración regional de Perú
        return String.format(loc, "%,.2f", n)  // Formatear como moneda
    }
}
