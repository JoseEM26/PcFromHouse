package com.cibertec.ahorraya.Repository

import android.content.ContentValues
import android.content.Context
import com.cibertec.ahorraya.Data.DbHelper

data class Movimiento(
    val id:Int =0,
    val tipo:String,
    val monto:Double,
    val fechaMillis:Long,
    val descripcion:String?,
    val synced :Boolean
)

class MovimientoSqlRepository (context: Context){
    private val helper=DbHelper(context)

    //Insertar un movimiento usando ContextValues
    fun insert(m:Movimiento){
        val db=helper.writableDatabase
        val cv=ContentValues().apply {
            put("tipo", m.tipo)
            put("monto", m.monto)
            put("fechaMillis", m.fechaMillis)
            put("descripcion", m.descripcion)
            put("synced", if(m.synced)1 else 0)
        }
        db.insert("movimientos",null,cv)
        db.close()
    }

    fun eliminar(id:Int){
        val db=helper.writableDatabase
        db.delete("movimientos","id=?", arrayOf(id.toString()))
        db.close()
    }

    fun listar(q:String ? =null):List<Movimiento>{
        val db=helper.readableDatabase
        val items= mutableListOf<Movimiento>()
        val(sel,args) =if (q.isNullOrBlank()) ("1=1" to emptyArray())
                      else ("descripcion LIKE ?" to arrayOf("%$q%"))

        val c=db.query(
            "movimientos",
            arrayOf("id","tipo","monto","fechaMillis","descripcion","synced"),
            sel,args,null,null,"fechaMillis DESC"
        )

        c.use {
            while(it.moveToNext()){
                items.add(
                    Movimiento(
                        id=it.getInt(0),
                        tipo=it.getString(1),
                        monto=it.getDouble(2),
                        fechaMillis = it.getLong(3),
                        descripcion = it.getString(4),
                        synced = it.getInt(5)==1
                    )
                )
            }
        }
        db.close()
        return items
    }

    fun sumIngreso():Double{
        val db=helper.readableDatabase
        val c=db.rawQuery("Select IFNULL(SUM(monto),0) FROM movimientos where tipo= 'INGRESO'" ,null)
        c.moveToFirst()
        val total=c.getDouble(0)
        c.close()
        return total
    }

    fun sumaGastos():Double{
        val db=helper.readableDatabase
        val c=db.rawQuery("Select IFNULL(SUM(monto),0) FROM movimientos where tipo= 'GASTO'" ,null)
        c.moveToFirst()
        val total=c.getDouble(0)
        c.close()
        return total
    }

    //marcar todos los registros no sincronisados
    fun marcarSincronizadosPendientes(){
        val db=helper.writableDatabase
        val cv=ContentValues().apply { put("synced", 1) }
        db.update("movimientos",cv,"synced=0",null)
        db.close()
    }

}