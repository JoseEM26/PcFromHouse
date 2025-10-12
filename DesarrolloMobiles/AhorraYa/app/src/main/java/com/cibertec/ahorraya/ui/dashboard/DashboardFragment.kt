package com.cibertec.ahorraya.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cibertec.ahorraya.R
import com.cibertec.ahorraya.databinding.FragmentDashboardBinding
import com.cibertec.ahorraya.ui.main.MainActivity
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.File

/**
 * DashboardFragment
 * - Muestra totales (ingresos, gastos y saldo)
 * - Exporta CSV en background
 * - Simula sincronización en background
 */
class DashboardFragment : Fragment() {

    // ViewBinding del layout fragment_dashboard
    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    // Acceso al repositorio desde la Activity
    private val repo by lazy { (requireActivity() as MainActivity).repo }

    // Alcance de corrutinas ligado a la vista
    private val ui = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarTotales()

        // Exportar CSV (trabajo de E/S en hilo de fondo)
        b.btnExportar.setOnClickListener { exportarCSV() }

        // Sincronizar (simulación con delay + update de flag 'synced')
        b.btnSincronizar.setOnClickListener {
            b.progress.visibility = View.VISIBLE
            ui.launch {
                withContext(Dispatchers.IO) {
                    delay(1500) // simula espera de red
                    repo.marcarSincronizadosPendientes()
                }
                b.progress.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_sinc_ok),
                    Toast.LENGTH_SHORT
                ).show()
                cargarTotales()
            }
        }
    }

    // Calcula ingresos, gastos y saldo sin bloquear la UI
    private fun cargarTotales() {
        ui.launch {
            val ingresos = withContext(Dispatchers.IO) { repo.sumIngresos() }
            val gastos   = withContext(Dispatchers.IO) { repo.sumGastos() }
            val saldo    = ingresos - gastos
            b.txtIngresosValor.text = getString(R.string.fmt_monedas, ingresos)
            b.txtGastosValor.text   = getString(R.string.fmt_monedas, gastos)
            b.txtSaldoValor.text    = getString(R.string.fmt_monedas, saldo)
        }
    }

    // Genera un CSV simple con los movimientos en cacheDir
    private fun exportarCSV() {
        ui.launch(Dispatchers.IO) {
            val file = File(requireContext().cacheDir, "movimientos.csv")

            // Tipamos explícitamente el writer para evitar problemas de inferencia en use { }
            val writer: BufferedWriter = file.bufferedWriter()
            writer.use { w ->
                // Encabezado
                w.write("tipo,monto,fechaMillis,descripcion,synced\n")

                // Filas (escapar comillas en descripción y terminar cada línea con \n)
                for (m in repo.listar(null)) {
                    val descripcion = m.descripcion?.replace("\"", "\"\"") ?: ""
                    w.write("${m.tipo},${m.monto},${m.fechaMillis},\"$descripcion\",${if (m.synced) 1 else 0}\n")
                }
                w.flush()
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_exportado_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.cancel() // cancela corrutinas para evitar fugas
        _b = null
    }
}
