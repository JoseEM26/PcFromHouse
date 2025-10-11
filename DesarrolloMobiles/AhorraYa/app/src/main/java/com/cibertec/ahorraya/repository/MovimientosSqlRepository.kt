package com.cibertec.ahorraya.repository

import android.content.ContentValues
import android.content.Context
import com.cibertec.ahorraya.data.DbHelper

/**
 * Data class Movimiento
 * - Representa un registro de la tabla 'movimientos'
 */
data class Movimiento(
    val id: Int = 0,
    val tipo: String,          // 'INGRESO' o 'GASTO'
    val monto: Double,         // cantidad
    val fechaMillis: Long,     // fecha (epoch millis)
    val descripcion: String?,  // texto opcional
    val synced: Boolean        // sincronizado o no
)

/**
 * MovimientosSqlRepository
 * - Encapsula CRUD + consultas de agregación sobre SQLite
 * - Evita que la UI conozca SQL directamente
 */
class MovimientosSqlRepository(context: Context) {
    private val helper = DbHelper(context)

    // Inserta un movimiento usando ContentValues
    fun insertar(m: Movimiento) {
        val db = helper.writableDatabase
        val cv = ContentValues().apply {
            put("tipo", m.tipo)
            put("monto", m.monto)
            put("fechaMillis", m.fechaMillis)
            put("descripcion", m.descripcion)
            put("synced", if (m.synced) 1 else 0)
        }
        db.insert("movimientos", null, cv)
        db.close()
    }

    // Elimina por id
    fun eliminar(id: Int) {
        val db = helper.writableDatabase
        db.delete("movimientos", "id=?", arrayOf(id.toString()))
        db.close()
    }

    // Lista todos o filtra por descripción usando LIKE
    fun listar(q: String? = null): List<Movimiento> {
        val db = helper.readableDatabase
        val items = mutableListOf<Movimiento>()
        val (sel, args) = if (q.isNullOrBlank()) ("1=1" to emptyArray())
                          else ("descripcion LIKE ?" to arrayOf("%$q%"))

        val c = db.query(
            "movimientos",
            arrayOf("id", "tipo", "monto", "fechaMillis", "descripcion", "synced"),
            sel, args, null, null, "fechaMillis DESC"
        )
        c.use {
            while (it.moveToNext()) {
                items.add(
                    Movimiento(
                        id = it.getInt(0),
                        tipo = it.getString(1),
                        monto = it.getDouble(2),
                        fechaMillis = it.getLong(3),
                        descripcion = it.getString(4),
                        synced = it.getInt(5) == 1
                    )
                )
            }
        }
        db.close()
        return items
    }

    // Suma de ingresos (para dashboard)
    fun sumIngresos(): Double {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT IFNULL(SUM(monto),0) FROM movimientos WHERE tipo='INGRESO'", null)
        c.moveToFirst()
        val total = c.getDouble(0)
        c.close(); db.close()
        return total
    }

    // Suma de gastos (para dashboard)
    fun sumGastos(): Double {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT IFNULL(SUM(monto),0) FROM movimientos WHERE tipo='GASTO'", null)
        c.moveToFirst()
        val total = c.getDouble(0)
        c.close(); db.close()
        return total
    }

    // Marca todos los registros no sincronizados como sincronizados
    fun marcarSincronizadosPendientes() {
        val db = helper.writableDatabase
        val cv = ContentValues().apply { put("synced", 1) }
        db.update("movimientos", cv, "synced=0", null)
        db.close()
    }
}