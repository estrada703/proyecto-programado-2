package cr.ac.monkeystore.movil.datos.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import cr.ac.monkeystore.movil.util.Bitacora;

/** Caché del listado en SQLite (una sola fila con clave "listado"). */
public class CacheListadoSqlite implements CacheListado {
    private static final String CLAVE = "listado";
    private final AdminDbHelper ayudante;

    public CacheListadoSqlite(AdminDbHelper ayudante) {
        this.ayudante = ayudante;
    }

    @Override
    public void guardar(String json) {
        try {
            ContentValues v = new ContentValues();
            v.put("clave", CLAVE);
            v.put("json", json);
            v.put("fecha", System.currentTimeMillis());
            ayudante.getWritableDatabase().insertWithOnConflict(AdminDbHelper.TABLA_CACHE, null, v,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "No se pudo guardar la caché del listado", e);
        }
    }

    private Cursor consultar() {
        return ayudante.getReadableDatabase().query(AdminDbHelper.TABLA_CACHE,
                new String[]{"json", "fecha"}, "clave = ?", new String[]{CLAVE}, null, null, null);
    }

    @Override
    public String leerJson() {
        try (Cursor c = consultar()) {
            return c.moveToFirst() ? c.getString(0) : null;
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "No se pudo leer la caché", e);
            return null;
        }
    }

    @Override
    public long leerFecha() {
        try (Cursor c = consultar()) {
            return c.moveToFirst() ? c.getLong(1) : 0L;
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "No se pudo leer la fecha de la caché", e);
            return 0L;
        }
    }

    @Override
    public void limpiar() {
        try {
            ayudante.getWritableDatabase().delete(AdminDbHelper.TABLA_CACHE, null, null);
        } catch (SQLiteException e) {
            Bitacora.e(Bitacora.TAG_BD, "No se pudo limpiar la caché", e);
        }
    }
}
