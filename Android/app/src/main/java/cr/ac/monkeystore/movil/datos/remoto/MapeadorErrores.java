package cr.ac.monkeystore.movil.datos.remoto;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/** Traduce un VolleyError a un {@link ErrorApi} (200 / 400 / 401 / 403 / red). */
public final class MapeadorErrores {
    private MapeadorErrores() { }

    public static ErrorApi desde(VolleyError error) {
        if (error.networkResponse != null) {
            int codigo = error.networkResponse.statusCode;
            if (codigo == 400) return new ErrorApi(ErrorApi.Tipo.PETICION_INVALIDA);
            if (codigo == 401) return new ErrorApi(ErrorApi.Tipo.NO_AUTORIZADO);
            if (codigo == 403) return new ErrorApi(ErrorApi.Tipo.PROHIBIDO);
            if (codigo == 404) return new ErrorApi(ErrorApi.Tipo.NO_ENCONTRADO);
            if (codigo == 409) return new ErrorApi(ErrorApi.Tipo.CONFLICTO);
            if (codigo >= 500) return new ErrorApi(ErrorApi.Tipo.SERVIDOR);
            return new ErrorApi(ErrorApi.Tipo.DESCONOCIDO);
        }
        if (error instanceof TimeoutError) return new ErrorApi(ErrorApi.Tipo.TIEMPO_AGOTADO);
        if (error instanceof NoConnectionError || error instanceof NetworkError) {
            return new ErrorApi(ErrorApi.Tipo.RED);
        }
        if (error instanceof ParseError) return new ErrorApi(ErrorApi.Tipo.RESPUESTA_INVALIDA);
        return new ErrorApi(ErrorApi.Tipo.DESCONOCIDO);
    }
}
