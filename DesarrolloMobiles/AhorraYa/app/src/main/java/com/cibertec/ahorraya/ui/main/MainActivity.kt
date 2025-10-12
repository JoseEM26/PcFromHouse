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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}