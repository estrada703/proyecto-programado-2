package cr.ac.sabortico.movil.datos.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.Locale;

import cr.ac.sabortico.movil.seguridad.HashClave;
import cr.ac.sabortico.movil.util.Bitacora;

/** Implementación SQLite. La contraseña se guarda como resumen PBKDF2 con sal. */
public class RepositorioCredencialesSqlite implements RepositorioCredenciales {
    private final AdminDbHelper ayudante;
    private final HashClave hash;

    public RepositorioCredencialesSqlite(AdminDbHelper ayudante, HashClave hash) {
        this.ayudante = ayudante;
        this.hash = hash;
    }

    private static String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public ResultadoRegistro registrar(String nombre, String correo, String clave) {
        String c = normalizar(correo);
        if (existe(c)) {
            return ResultadoRegistro.YA_EXISTE;
        }
        try {
            String sal = hash.generarSal();
            ContentValues v = new ContentValues();
            v.put("nombre", nombre.trim());
            v.put("correo", c);
            v.put("sal", sal);
            v.put("hash", hash.resumir(clave, sal));
            long id = ayudante.getWritableDatabase().insert(AdminDbHelper.TABLA_ADMIN, null, v);
            return id == -1 ? ResultadoRegistro.ERROR : ResultadoRegistro.REGISTRADO;
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "No se pudo registrar al administrador", e);
            return ResultadoRegistro.ERROR;
        }
    }

    @Override
    public boolean existe(String correo) {
        try {
            SQLiteDatabase db = ayudante.getReadableDatabase();
            try (Cursor cur = db.query(AdminDbHelper.TABLA_ADMIN, new String[]{"id"},
                    "correo = ?", new String[]{normalizar(correo)}, null, null, null)) {
                return cur.moveToFirst();
            }
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "Error consultando administrador", e);
            return false;
        }
    }

    @Override
    public boolean verificar(String correo, String clave) {
        try {
            SQLiteDatabase db = ayudante.getReadableDatabase();
            try (Cursor cur = db.query(AdminDbHelper.TABLA_ADMIN, new String[]{"sal", "hash"},
                    "correo = ?", new String[]{normalizar(correo)}, null, null, null)) {
                return cur.moveToFirst() && hash.verificar(clave, cur.getString(0), cur.getString(1));
            }
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "Error verificando credenciales", e);
            return false;
        }
    }

    @Override
    public String nombrePorCorreo(String correo) {
        try {
            SQLiteDatabase db = ayudante.getReadableDatabase();
            try (Cursor cur = db.query(AdminDbHelper.TABLA_ADMIN, new String[]{"nombre"},
                    "correo = ?", new String[]{normalizar(correo)}, null, null, null)) {
                return cur.moveToFirst() ? cur.getString(0) : null;
            }
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "Error leyendo nombre", e);
            return null;
        }
    }
}
