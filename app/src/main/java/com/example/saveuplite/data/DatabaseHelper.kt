package com.example.saveuplite.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.saveuplite.model.Usuario
import java.util.Date

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "saveup.db"
        private const val DATABASE_VERSION = 5 // <-- VERSIÓN INCREMENTADA

        // Tablas
        const val TABLE_FORM = "usuarios"
        const val TABLE_AUTH = "auth_usuarios"

        // Columnas
        const val COL_ID = "id"
        const val COL_NOMBRE_FORM = "nombre"
        const val COL_RUT_FORM = "rut"
        const val COL_INGRESO = "ingreso"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_AUTH_RUT = "rut"
        const val COL_AUTH_NOMBRE = "nombre"
        const val COL_AUTH_APELLIDO = "apellido"
        const val COL_AUTH_EMAIL = "email"
        const val COL_AUTH_CONTRASENA = "contrasena"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableForm = "CREATE TABLE $TABLE_FORM ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NOMBRE_FORM TEXT NOT NULL, $COL_RUT_FORM TEXT NOT NULL, $COL_INGRESO INTEGER NOT NULL, $COL_DESCRIPCION TEXT)"
        val createTableAuth = "CREATE TABLE $TABLE_AUTH ($COL_AUTH_RUT TEXT PRIMARY KEY, $COL_AUTH_NOMBRE TEXT NOT NULL, $COL_AUTH_APELLIDO TEXT NOT NULL, $COL_AUTH_EMAIL TEXT NOT NULL UNIQUE, $COL_AUTH_CONTRASENA TEXT NOT NULL)"

        db.execSQL(createTableForm)
        db.execSQL(createTableAuth)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FORM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUTH")
        db.execSQL("DROP TABLE IF EXISTS saldos") // <-- Tabla obsoleta eliminada
        onCreate(db)
    }

    // --- Métodos TBL_AUTH (Se mantienen por si hay lógica de login local remanente) ---
    fun insertarAuthUsuario(usuario: Usuario): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_AUTH_RUT, usuario.rut)
            put(COL_AUTH_NOMBRE, usuario.nombre)
            put(COL_AUTH_APELLIDO, usuario.apellido)
            put(COL_AUTH_EMAIL, usuario.email)
            put(COL_AUTH_CONTRASENA, usuario.contrasena)
        }
        val result = db.insert(TABLE_AUTH, null, values)
        return result != -1L
    }

    fun obtenerAuthUsuarioPorEmail(email: String): Usuario? {
        val db = readableDatabase
        val cursor = db.query(TABLE_AUTH, null, "$COL_AUTH_EMAIL = ?", arrayOf(email), null, null, null)
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                rut = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_RUT)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_NOMBRE)),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_APELLIDO)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_EMAIL)),
                contrasena = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_CONTRASENA)),
                fechaRegistro = Date()
            )
        }
        cursor.close()
        return usuario
    }

    fun usuarioExistePorRut(rut: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_AUTH, arrayOf(COL_AUTH_RUT), "$COL_AUTH_RUT = ?", arrayOf(rut), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun usuarioExistePorEmail(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_AUTH, arrayOf(COL_AUTH_EMAIL), "$COL_AUTH_EMAIL = ?", arrayOf(email), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // --- Otros métodos (Legacy) ---
    fun insertarUsuario(nombre: String, rut: String, ingreso: Int, descripcion: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMBRE_FORM, nombre)
            put(COL_RUT_FORM, rut)
            put(COL_INGRESO, ingreso)
            put(COL_DESCRIPCION, descripcion)
        }
        return db.insert(TABLE_FORM, null, values) != -1L
    }

    fun obtenerUsuarios(): List<UsuarioDB> {
        val lista = mutableListOf<UsuarioDB>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FORM", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(UsuarioDB(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_FORM)),
                    rut = cursor.getString(cursor.getColumnIndexOrThrow(COL_RUT_FORM)),
                    ingreso = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INGRESO)),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}

data class UsuarioDB(
    val id: Int,
    val nombre: String,
    val rut: String,
    val ingreso: Int,
    val descripcion: String
)
