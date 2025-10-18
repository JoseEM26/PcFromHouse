package com.cibertec.ahorraya.ui.nuevo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.ahorraya.R
import com.cibertec.ahorraya.databinding.FragmentNuevoMovimientoBinding
import com.cibertec.ahorraya.repository.Movimiento
import com.cibertec.ahorraya.ui.main.MainActivity
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*

/**
 * NuevoMovimientoFragment
 * - Formulario para registrar ingresos/gastos
 * - Usa MaterialDatePicker para la fecha
 * - Valida monto y guarda en SQLite en background
 */
class NuevoMovimientoFragment : Fragment() {

    private var _b: FragmentNuevoMovimientoBinding? = null
    private val b get() = _b!!

    private val repo by lazy { (requireActivity() as MainActivity).repo }
    private val ui = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Fecha seleccionada por defecto: ahora
    private var fechaSeleccionMillis: Long = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentNuevoMovimientoBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Crea el listener que abre el DatePicker
        val abrirPicker = View.OnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona la fecha")
                .setSelection(fechaSeleccionMillis)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                // 'selection' es epoch millis en UTC
                fechaSeleccionMillis = selection
                // Muestra un texto amigable en el campo
                b.tilFecha.editText?.setText(picker.headerText)
                b.tilFecha.editText?.contentDescription = "Fecha seleccionada: ${picker.headerText}"
            }
            picker.show(parentFragmentManager, "dp")
        }

        // Abrir el datepicker desde el icono y el campo
        b.tilFecha.setEndIconOnClickListener(abrirPicker)
        b.etFecha.setOnClickListener(abrirPicker)

        // Guardar movimiento
        b.btnGuardar.setOnClickListener {
            val tipo = if (b.rbIngreso.isChecked) "INGRESO" else "GASTO"

            // Lee y valida el monto
            val montoTxt = b.etMonto.text?.toString()?.trim().orEmpty()
            val monto = montoTxt.toDoubleOrNull()
            if (monto == null || monto <= 0.0) {
                b.tilMonto.error = getString(R.string.msg_monto_obligatorio)
                b.etMonto.requestFocus()
                return@setOnClickListener
            } else {
                b.tilMonto.error = null
            }

            val desc = b.etDescripcion.text?.toString()?.ifBlank { null }

            // Inserta en background para no bloquear la UI
            ui.launch(Dispatchers.IO) {
                repo.insertar(
                    Movimiento(
                        tipo = tipo,
                        monto = monto!!,
                        fechaMillis = fechaSeleccionMillis,
                        descripcion = desc,
                        synced = false
                    )
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.msg_guardado_ok), Toast.LENGTH_SHORT).show()
                    // Limpia el formulario (menos la fecha)
                    b.etMonto.text?.clear()
                    b.etDescripcion.text?.clear()
                    b.rbIngreso.isChecked = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.cancel()
        _b = null
    }
}