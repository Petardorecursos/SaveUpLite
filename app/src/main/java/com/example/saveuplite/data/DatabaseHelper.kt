package com.example.saveuplite.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "saveup.db"
        private const val DATABASE_VERSION = 1

        // Nombre de la tabla y columnas
        const val TABLE_NAME = "usuarios"
        const val COL_ID = "id"
        const val COL_NOMBRE = "nombre"
        const val COL_RUT = "rut"
        const val COL_INGRESO = "ingreso"
        const val COL_DESCRIPCION = "descripcion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_RUT TEXT NOT NULL,
                $COL_INGRESO INTEGER NOT NULL,
                $COL_DESCRIPCION TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // 🟩 Función para insertar un registro
    fun insertarUsuario(nombre: String, rut: String, ingreso: Int, descripcion: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_RUT, rut)
            put(COL_INGRESO, ingreso)
            put(COL_DESCRIPCION, descripcion)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }
}
