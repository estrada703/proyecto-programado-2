package cr.ac.monkeystore.movil.datos.remoto;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import cr.ac.monkeystore.movil.modelo.CompraParser;
import cr.ac.monkeystore.movil.util.Bitacora;

/** GET /api/movil/compras con el token de sesión en el encabezado Authorization. */
public class ServicioComprasVolley implements ServicioCompras {
    private final RequestQueue cola;
    private final String urlBase;

    public ServicioComprasVolley(RequestQueue cola, String urlBase) {
        this.cola = cola;
        this.urlBase = urlBase;
    }

    @Override
    public void obtenerCompras(final String token, final Callback<ResultadoCompras> callback) {
        JsonArrayRequest peticion = new JsonArrayRequest(Request.Method.GET,
                urlBase + "api/movil/compras", null,
                respuesta -> {
                    try {
                        Bitacora.i(Bitacora.TAG_LISTADO, "Listado recibido: " + respuesta.length() + " filas");
                        callback.alExito(new ResultadoCompras(CompraParser.parsear(respuesta), respuesta.toString()));
                    } catch (JSONException e) {
                        Bitacora.e(Bitacora.TAG_LISTADO, "JSON del listado inválido", e);
                        callback.alError(new ErrorApi(ErrorApi.Tipo.RESPUESTA_INVALIDA));
                    }
                },
                error -> {
                    ErrorApi e = MapeadorErrores.desde(error);
                    Bitacora.w(Bitacora.TAG_LISTADO, "Error " + e.getTipo() + " cargando el listado");
                    callback.alError(e);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> encabezados = new HashMap<>();
                encabezados.put("Authorization", "Bearer " + token);
                return encabezados;
            }
        };
        peticion.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));
        cola.add(peticion);
    }
}
