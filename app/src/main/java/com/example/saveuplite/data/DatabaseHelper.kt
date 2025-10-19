package com.example.saveuplite.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.saveuplite.model.Usuario

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "saveup.db"
        private const val DATABASE_VERSION = 1

        // Tabla para el formulario original
        const val TABLE_FORM = "usuarios"
        const val COL_ID = "id"
        const val COL_NOMBRE_FORM = "nombre"
        const val COL_RUT_FORM = "rut"
        const val COL_INGRESO = "ingreso"
        const val COL_DESCRIPCION = "descripcion"

        // --- Nueva tabla para autenticación ---
        const val TABLE_AUTH = "auth_usuarios"
        const val COL_AUTH_RUT = "rut"
        const val COL_AUTH_NOMBRE = "nombre"
        const val COL_AUTH_APELLIDO = "apellido"
        const val COL_AUTH_EMAIL = "email"
        const val COL_AUTH_CONTRASENA = "contrasena"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla original del formulario
        val createTableForm = """
            CREATE TABLE $TABLE_FORM (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE_FORM TEXT NOT NULL,
                $COL_RUT_FORM TEXT NOT NULL,
                $COL_INGRESO INTEGER NOT NULL,
                $COL_DESCRIPCION TEXT
            )
        """.trimIndent()
        db.execSQL(createTableForm)

        // --- Crear nueva tabla de autenticación ---
        val createTableAuth = """
            CREATE TABLE $TABLE_AUTH (
                $COL_AUTH_RUT TEXT PRIMARY KEY,
                $COL_AUTH_NOMBRE TEXT NOT NULL,
                $COL_AUTH_APELLIDO TEXT NOT NULL,
                $COL_AUTH_EMAIL TEXT NOT NULL UNIQUE,
                $COL_AUTH_CONTRASENA TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableAuth)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FORM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUTH") // Limpiar también la nueva tabla
        onCreate(db)
    }

    // --- Métodos para la tabla del formulario (sin cambios) ---
    fun insertarUsuario(nombre: String, rut: String, ingreso: Int, descripcion: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMBRE_FORM, nombre)
            put(COL_RUT_FORM, rut)
            put(COL_INGRESO, ingreso)
            put(COL_DESCRIPCION, descripcion)
        }
        val result = db.insert(TABLE_FORM, null, values)
        db.close()
        return result != -1L
    }

    fun obtenerUsuarios(): List<UsuarioDB> {
        val lista = mutableListOf<UsuarioDB>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FORM", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_FORM))
                val rut = cursor.getString(cursor.getColumnIndexOrThrow(COL_RUT_FORM))
                val ingreso = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INGRESO))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPCION))
                lista.add(UsuarioDB(id, nombre, rut, ingreso, descripcion))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- Nuevos métodos para la tabla de autenticación ---

    /**
     * Inserta un nuevo usuario en la tabla de autenticación.
     */
    fun insertarAuthUsuario(usuario: Usuario): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_AUTH_RUT, usuario.rut)
            put(COL_AUTH_NOMBRE, usuario.nombre)
            put(COL_AUTH_APELLIDO, usuario.apellido)
            put(COL_AUTH_EMAIL, usuario.email)
            put(COL_AUTH_CONTRASENA, usuario.contrasena) // En una app real, aquí iría el hash
        }
        val result = db.insert(TABLE_AUTH, null, values)
        db.close()
        return result != -1L
    }

    /**
     * Busca un usuario de autenticación por su email.
     * Devuelve un objeto Usuario si lo encuentra, si no, devuelve null.
     */
    fun obtenerAuthUsuarioPorEmail(email: String): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_AUTH,
            arrayOf(COL_AUTH_RUT, COL_AUTH_NOMBRE, COL_AUTH_APELLIDO, COL_AUTH_EMAIL, COL_AUTH_CONTRASENA),
            "$COL_AUTH_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            val rut = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_RUT))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_NOMBRE))
            val apellido = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_APELLIDO))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_EMAIL))
            val contrasena = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTH_CONTRASENA))
            // La fecha de registro no está en la BD, así que la creamos al momento.
            // En un sistema real, este campo también estaría en la tabla.
            usuario = Usuario(rut, nombre, apellido, userEmail, contrasena, java.util.Date())
        }

        cursor.close()
        db.close()
        return usuario
    }
}

// Clase de datos para los registros del formulario
data class UsuarioDB(
    val id: Int,
    val nombre: String,
    val rut: String,
    val ingreso: Int,
    val descripcion: String
)
