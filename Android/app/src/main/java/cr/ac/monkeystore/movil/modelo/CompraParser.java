package cr.ac.monkeystore.movil.modelo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Convierte el JSON de la Web API en objetos {@link Compra}. */
public final class CompraParser {

    private CompraParser() { }

    /**
     * @param arreglo arreglo JSON devuelto por GET /api/movil/compras
     * @return lista de compras (vacía si el arreglo está vacío)
     * @throws JSONException si falta un campo obligatorio
     */
    public static List<Compra> parsear(JSONArray arreglo) throws JSONException {
        List<Compra> lista = new ArrayList<>();
        for (int i = 0; i < arreglo.length(); i++) {
            JSONObject o = arreglo.getJSONObject(i);
            lista.add(new Compra(
                    o.getInt("id"),
                    o.getString("producto"),
                    o.optString("descripcion", ""),
                    o.getDouble("precio"),
                    o.getInt("cantidad"),
                    o.getString("fecha"),
                    o.getDouble("monto"),
                    o.optString("catalogo", ""),
                    o.optString("tipo", ""),
                    o.optString("pedido", ""),
                    o.optString("estado", ""),
                    o.optString("condicion", ""),
                    o.optString("cliente", ""),
                    o.optBoolean("reserva", false)));
        }
        return lista;
    }

    /** Variante para cadenas JSON (se usa en la caché sin conexión). */
    public static List<Compra> parsear(String json) throws JSONException {
        return parsear(new JSONArray(json));
    }
}
