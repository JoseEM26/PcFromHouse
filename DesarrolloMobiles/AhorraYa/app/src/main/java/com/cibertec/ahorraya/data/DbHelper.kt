package com.cibertec.ahorraya.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * DbHelper
 * - Crea y actualiza la base de datos local 'ahorraya.db'
 * - Define la tabla 'movimientos' para persistencia
 */
class DbHelper(context: Context): SQLiteOpenHelper(context, "ahorraya.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crea la tabla principal con índices de búsqueda
        db.execSQL("""
            CREATE TABLE movimientos(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,         -- 'INGRESO' o 'GASTO'
                monto REAL NOT NULL,        -- cantidad en decimal
                fechaMillis INTEGER NOT NULL, -- fecha en milisegundos epoch
                descripcion TEXT,           -- opcional
                synced INTEGER NOT NULL DEFAULT 0 -- flag para 'sincronizado'
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX idx_mov_fecha ON movimientos(fechaMillis)")
        db.execSQL("CREATE INDEX idx_mov_desc  ON movimientos(descripcion)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Aquí irían migraciones si incrementas la versión
    }
}