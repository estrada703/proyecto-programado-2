package cr.ac.monkeystore.movil.datos.remoto;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cr.ac.monkeystore.movil.modelo.Producto;
import cr.ac.monkeystore.movil.modelo.ProductoParser;
import cr.ac.monkeystore.movil.util.Bitacora;

/** Implementación con Volley; las pantallas solo conocen {@link ServicioGestion}. */
public class ServicioGestionVolley implements ServicioGestion {
    private static final String TAG = Bitacora.TAG_GESTION;
    private final RequestQueue cola;
    private final String urlBase;

    public ServicioGestionVolley(RequestQueue cola, String urlBase) {
        this.cola = cola;
        this.urlBase = urlBase;
    }

    private static Map<String, String> encabezados(String token) {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + token);
        return h;
    }

    private static <T> void error(com.android.volley.VolleyError e, String accion, Callback<T> cb) {
        ErrorApi err = MapeadorErrores.desde(e);
        Bitacora.w(TAG, "Error " + err.getTipo() + " al " + accion);
        cb.alError(err);
    }

    private <R extends Request<?>> void encolar(R peticion) {
        // PUT no es idempotente para el usuario si se repite tras un error: sin reintentos automáticos.
        int reintentos = peticion.getMethod() == Request.Method.GET ? 1 : 0;
        peticion.setRetryPolicy(new DefaultRetryPolicy(10000, reintentos, 1.0f));
        cola.add(peticion);
    }

    @Override
    public void cambiarEstadoPedido(final String token, int pedidoId, String estado, final Callback<String> cb) {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("estado", estado);
        } catch (JSONException e) {
            cb.alError(new ErrorApi(ErrorApi.Tipo.DESCONOCIDO));
            return;
        }
        encolar(new JsonObjectRequest(Request.Method.PUT,
                urlBase + "api/movil/pedidos/" + pedidoId + "/estado", cuerpo,
                r -> {
                    String nuevo = r.optString("estado", estado);
                    Bitacora.i(TAG, "Pedido " + pedidoId + " ahora está " + nuevo);
                    cb.alExito(nuevo);
                },
                e -> error(e, "cambiar el estado del pedido", cb)) {
            @Override
            public Map<String, String> getHeaders() { return encabezados(token); }
        });
    }

    @Override
    public void obtenerProductos(final String token, final Callback<List<Producto>> cb) {
        encolar(new JsonArrayRequest(Request.Method.GET, urlBase + "api/movil/productos", null,
                r -> {
                    try {
                        Bitacora.i(TAG, "Inventario recibido: " + r.length() + " productos");
                        cb.alExito(ProductoParser.parsear(r));
                    } catch (JSONException e) {
                        Bitacora.e(TAG, "JSON del inventario inválido", e);
                        cb.alError(new ErrorApi(ErrorApi.Tipo.RESPUESTA_INVALIDA));
                    }
                },
                e -> error(e, "cargar el inventario", cb)) {
            @Override
            public Map<String, String> getHeaders() { return encabezados(token); }
        });
    }

    @Override
    public void actualizarStock(final String token, int productoId, int stock, final Callback<Producto> cb) {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("stock", stock);
        } catch (JSONException e) {
            cb.alError(new ErrorApi(ErrorApi.Tipo.DESCONOCIDO));
            return;
        }
        encolar(new JsonObjectRequest(Request.Method.PUT,
                urlBase + "api/movil/productos/" + productoId + "/stock", cuerpo,
                r -> {
                    try {
                        Bitacora.i(TAG, "Stock del producto " + productoId + " actualizado a " + stock);
                        cb.alExito(ProductoParser.parsear(r));
                    } catch (JSONException e) {
                        Bitacora.e(TAG, "JSON del producto inválido", e);
                        cb.alError(new ErrorApi(ErrorApi.Tipo.RESPUESTA_INVALIDA));
                    }
                },
                e -> error(e, "actualizar el stock", cb)) {
            @Override
            public Map<String, String> getHeaders() { return encabezados(token); }
        });
    }
}
