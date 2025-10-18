package com.cibertec.ahorraya.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cibertec.ahorraya.R
import com.cibertec.ahorraya.databinding.ActivityMainBinding
import com.cibertec.ahorraya.repository.MovimientosSqlRepository
import com.cibertec.ahorraya.ui.dashboard.DashboardFragment
import com.cibertec.ahorraya.ui.historial.HistorialFragment
import com.cibertec.ahorraya.ui.nuevo.NuevoMovimientoFragment

/**
 * MainActivity
 * - Configura la Toolbar Material 3
 * - Inicializa el repositorio SQLite
 * - Maneja la navegación inferior (3 fragments)
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding para acceder a las vistas de activity_main.xml
    private lateinit var binding: ActivityMainBinding

    // Repositorio que encapsula el acceso a SQLite
    lateinit var repo: MovimientosSqlRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla el layout y lo muestra
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar Material como ActionBar
        setSupportActionBar(binding.toolbar)

        // Crea el repositorio SQLite (DbHelper por dentro)
        repo = MovimientosSqlRepository(this)

        // Configura la navegación inferior para cargar cada fragment
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> { openFragment(DashboardFragment()); true }
                R.id.nav_nuevo -> { openFragment(NuevoMovimientoFragment()); true }
                R.id.nav_historial -> { openFragment(HistorialFragment()); true }
                else -> false
            }
        }

        // Selección por defecto (Inicio) solo la primera vez
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_inicio
        }
    }

    // Reemplaza el contenedor con el fragment solicitado
    private fun openFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit()
    }
}