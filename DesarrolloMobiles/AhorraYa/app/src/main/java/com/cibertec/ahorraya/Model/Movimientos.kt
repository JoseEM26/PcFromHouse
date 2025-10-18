package com.cibertec.ahorraya.Model

data class Movimientos (
    val id: Int = 0,
    val tipo: String,          // 'INGRESO' o 'GASTO'
    val monto: Double,         // cantidad
    val fechaMillis: Long,     // fecha (epoch millis)
    val descripcion: String?,  // texto opcional
    val synced: Boolean
)