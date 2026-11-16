package cr.ac.monkeystore.movil.util;

import android.content.Context;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;

/** Texto amable (desde res/values) para cada tipo de error. */
public final class Mensajes {
    private Mensajes() { }

    /** @param enSesion true si ya había ingresado (un 401 significa sesión vencida) */
    public static String paraError(Context c, ErrorApi error, boolean enSesion) {
        switch (error.getTipo()) {
            case PETICION_INVALIDA: return c.getString(R.string.api_400);
            case NO_AUTORIZADO:
                return c.getString(enSesion ? R.string.api_401_sesion : R.string.api_401_credenciales);
            case PROHIBIDO: return c.getString(R.string.api_403);
            case SERVIDOR: return c.getString(R.string.api_servidor);
            case RED: return c.getString(R.string.api_red);
            case TIEMPO_AGOTADO: return c.getString(R.string.api_tiempo);
            case RESPUESTA_INVALIDA: return c.getString(R.string.api_respuesta_invalida);
            case CIFRADO: return c.getString(R.string.msg_error_cifrado);
            default: return c.getString(R.string.api_desconocido);
        }
    }
}
