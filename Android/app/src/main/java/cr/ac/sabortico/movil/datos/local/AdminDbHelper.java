package cr.ac.sabortico.movil.datos.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Base de datos SQLite del dispositivo: administradores registrados y caché del listado. */
public class AdminDbHelper extends SQLiteOpenHelper {
    public static final String NOMBRE_BD = "sabortico_movil.db";
    public static final int VERSION = 1;

    public static final String TABLA_ADMIN = "administradores";
    public static final String TABLA_CACHE = "cache_listado";

    public AdminDbHelper(Context contexto) {
        super(contexto.getApplicationContext(), NOMBRE_BD, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_ADMIN + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre TEXT NOT NULL, "
                + "correo TEXT NOT NULL UNIQUE, "
                + "sal TEXT NOT NULL, "
                + "hash TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLA_CACHE + " ("
                + "clave TEXT PRIMARY KEY, "
                + "json TEXT NOT NULL, "
                + "fecha INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_ADMIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CACHE);
        onCreate(db);
    }
}
