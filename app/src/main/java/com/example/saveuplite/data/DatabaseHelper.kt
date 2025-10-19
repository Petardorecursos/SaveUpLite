package com.example.saveuplite.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.saveuplite.model.EventoSaldo
import com.example.saveuplite.model.Saldo
import com.example.saveuplite.model.Usuario
import java.util.Date

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "saveup.db"
        private const val DATABASE_VERSION = 2 // <-- ¡VERSION ACTUALIZADA A 2!

        // Tablas
        const val TABLE_FORM = "usuarios"
        const val TABLE_AUTH = "auth_usuarios"
        const val TABLE_SALDO = "saldos"

        // Columnas comunes
        const val COL_ID = "id"

        // Columnas TBL_FORM
        const val COL_NOMBRE_FORM = "nombre"
        const val COL_RUT_FORM = "rut"
        const val COL_INGRESO = "ingreso"
        const val COL_DESCRIPCION = "descripcion"

        // Columnas TBL_AUTH
        const val COL_AUTH_RUT = "rut"
        const val COL_AUTH_NOMBRE = "nombre"
        const val COL_AUTH_APELLIDO = "apellido"
        const val COL_AUTH_EMAIL = "email"
        const val COL_AUTH_CONTRASENA = "contrasena"

        // Columnas TBL_SALDO
        const val COL_SALDO_ID = "id_saldo"
        const val COL_SALDO_MONTO = "monto"
        const val COL_SALDO_FECHA = "fecha_registro"
        const val COL_SALDO_RUT_USUARIO = "usuario_rut"
        const val COL_SALDO_TIPO_EVENTO = "tipo_evento"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableForm = "CREATE TABLE $TABLE_FORM ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NOMBRE_FORM TEXT NOT NULL, $COL_RUT_FORM TEXT NOT NULL, $COL_INGRESO INTEGER NOT NULL, $COL_DESCRIPCION TEXT)"
        val createTableAuth = "CREATE TABLE $TABLE_AUTH ($COL_AUTH_RUT TEXT PRIMARY KEY, $COL_AUTH_NOMBRE TEXT NOT NULL, $COL_AUTH_APELLIDO TEXT NOT NULL, $COL_AUTH_EMAIL TEXT NOT NULL UNIQUE, $COL_AUTH_CONTRASENA TEXT NOT NULL)"
        val createTableSaldo = """
            CREATE TABLE $TABLE_SALDO (
                $COL_SALDO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SALDO_MONTO REAL NOT NULL,
                $COL_SALDO_FECHA INTEGER NOT NULL, 
                $COL_SALDO_TIPO_EVENTO TEXT NOT NULL,
                $COL_SALDO_RUT_USUARIO TEXT NOT NULL,
                FOREIGN KEY($COL_SALDO_RUT_USUARIO) REFERENCES $TABLE_AUTH($COL_AUTH_RUT)
            )
        """.trimIndent()
        
        db.execSQL(createTableForm)
        db.execSQL(createTableAuth)
        db.execSQL(createTableSaldo)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FORM")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUTH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALDO")
        onCreate(db)
    }

    // --- Métodos TBL_AUTH ---
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
        db.close()
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
                fechaRegistro = Date() // La fecha no se guarda en la BD, se genera al momento
            )
        }
        cursor.close()
        db.close()
        return usuario
    }

    // --- Métodos TBL_FORM ---
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
        db.close()
        return lista
    }

    // --- Métodos TBL_SALDO ---
    fun insertarSaldo(saldo: Saldo): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SALDO_MONTO, saldo.monto)
            put(COL_SALDO_FECHA, saldo.fechaRegistro.time)
            put(COL_SALDO_TIPO_EVENTO, saldo.tipoEvento.name)
            put(COL_SALDO_RUT_USUARIO, saldo.usuarioRut)
        }
        val result = db.insert(TABLE_SALDO, null, values)
        db.close()
        return result != -1L
    }

    fun obtenerSaldoActual(usuarioRut: String): Float {
        val db = readableDatabase
        val cursor = db.query(TABLE_SALDO, arrayOf(COL_SALDO_MONTO), "$COL_SALDO_RUT_USUARIO = ?", arrayOf(usuarioRut), null, null, "$COL_SALDO_FECHA DESC", "1")
        var saldoActual = 0f
        if (cursor.moveToFirst()) {
            saldoActual = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_SALDO_MONTO))
        }
        cursor.close()
        db.close()
        return saldoActual
    }

    fun obtenerSaldosPorUsuario(usuarioRut: String): List<Saldo> {
        val listaSaldos = mutableListOf<Saldo>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SALDO, null, "$COL_SALDO_RUT_USUARIO = ?", arrayOf(usuarioRut), null, null, "$COL_SALDO_FECHA DESC")
        if (cursor.moveToFirst()) {
            do {
                listaSaldos.add(Saldo(
                    idSaldo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALDO_ID)),
                    monto = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_SALDO_MONTO)),
                    fechaRegistro = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COL_SALDO_FECHA))),
                    tipoEvento = EventoSaldo.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_SALDO_TIPO_EVENTO))),
                    usuarioRut = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALDO_RUT_USUARIO))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listaSaldos
    }
}

data class UsuarioDB(
    val id: Int,
    val nombre: String,
    val rut: String,
    val ingreso: Int,
    val descripcion: String
)
