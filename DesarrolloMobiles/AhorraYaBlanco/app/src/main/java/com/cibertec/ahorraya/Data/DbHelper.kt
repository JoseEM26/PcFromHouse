package com.cibertec.ahorraya.Data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context :Context):SQLiteOpenHelper(context,"ahorrava.db",null ,1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            create table movimientos(
               id INTEGER primary key autoincrement,
               tipo text not null,
               monto real not null,
               fechaMillis integer not null,
               synced integer not null default 0
            )
        """.trimIndent())

        db.execSQL("create index idx_mov_fecha on movimientos(fechaMillis)")
        db.execSQL("create index idx_mov_desc on movimientos(descripcion)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

}