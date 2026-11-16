package cr.ac.sabortico.movil.datos.remoto;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;

import cr.ac.sabortico.movil.seguridad.Cifrador;
import cr.ac.sabortico.movil.util.Bitacora;

/**
 * Clase abstracta que comparten el registro y el ingreso: arma las credenciales,
 * las cifra con AES-256-GCM, envía { "datos": "..." } y clasifica la respuesta.
 * Cada subclase solo dice la ruta y cómo interpretar la respuesta.
 */
public abstract class PeticionCifrada {
    private static final int TIEMPO_ESPERA_MS = 10000;
    private static final int REINTENTOS = 1;

    private final RequestQueue cola;
    private final Cifrador cifrador;
    private final String urlBase;

    protected PeticionCifrada(RequestQueue cola, Cifrador cifrador, String urlBase) {
        this.cola = cola;
        this.cifrador = cifrador;
        this.urlBase = urlBase;
    }

    /** Ruta relativa, por ejemplo "api/movil/ingresar". */
    protected abstract String ruta();

    /** Convierte el JSON de respuesta 200 en un resultado. */
    protected abstract ResultadoAuth interpretar(JSONObject respuesta) throws JSONException;

    public void enviar(String correo, String clave, final Callback<ResultadoAuth> callback) {
        JSONObject cuerpo;
        try {
            JSONObject credenciales = new JSONObject();
            credenciales.put("correo", correo);
            credenciales.put("clave", clave);
            cuerpo = new JSONObject();
            cuerpo.put("datos", cifrador.cifrar(credenciales.toString()));
        } catch (GeneralSecurityException | JSONException e) {
            Bitacora.e(Bitacora.TAG_CIFRADO, "Falló el cifrado de las credenciales", e);
            callback.alError(new ErrorApi(ErrorApi.Tipo.CIFRADO));
            return;
        }

        final String url = urlBase + ruta();
        JsonObjectRequest peticion = new JsonObjectRequest(Request.Method.POST, url, cuerpo,
                respuesta -> {
                    try {
                        Bitacora.i(Bitacora.TAG_RED, "200 en " + ruta());
                        callback.alExito(interpretar(respuesta));
                    } catch (JSONException e) {
                        Bitacora.e(Bitacora.TAG_RED, "JSON inesperado en " + ruta(), e);
                        callback.alError(new ErrorApi(ErrorApi.Tipo.RESPUESTA_INVALIDA));
                    }
                },
                error -> {
                    ErrorApi e = MapeadorErrores.desde(error);
                    Bitacora.w(Bitacora.TAG_RED, "Error " + e.getTipo() + " en " + ruta());
                    callback.alError(e);
                });
        peticion.setRetryPolicy(new DefaultRetryPolicy(TIEMPO_ESPERA_MS, REINTENTOS, 1.0f));
        cola.add(peticion);
    }
}
