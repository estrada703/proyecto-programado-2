package cr.ac.monkeystore.movil.util;

import android.util.Log;

/**
 * Bitácora de la aplicación con etiquetas propias (T7).
 * NUNCA se debe pasar una contraseña a estos métodos.
 */
public final class Bitacora {
    public static final String TAG_REGISTRO = "MonkeyStore.Registro";
    public static final String TAG_INGRESO = "MonkeyStore.Ingreso";
    public static final String TAG_LISTADO = "MonkeyStore.Listado";
    public static final String TAG_RED = "MonkeyStore.Red";
    public static final String TAG_BD = "MonkeyStore.BD";
    public static final String TAG_CIFRADO = "MonkeyStore.Cifrado";

    private Bitacora() { }

    public static void i(String tag, String mensaje) { Log.i(tag, mensaje); }

    public static void w(String tag, String mensaje) { Log.w(tag, mensaje); }

    public static void e(String tag, String mensaje, Throwable causa) { Log.e(tag, mensaje, causa); }
}
