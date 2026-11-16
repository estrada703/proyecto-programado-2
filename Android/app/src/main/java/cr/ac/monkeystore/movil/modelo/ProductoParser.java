package cr.ac.monkeystore.movil.modelo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Convierte el JSON de GET /api/movil/productos en objetos {@link Producto}. */
public final class ProductoParser {
    private ProductoParser() { }

    public static Producto parsear(JSONObject o) throws JSONException {
        return new Producto(o.getInt("id"), o.getString("nombre"), o.optString("catalogo", ""),
                o.optString("tipo", ""), o.optString("condicion", ""), o.getDouble("precio"),
                o.getInt("stock"), o.optBoolean("preventa", false));
    }

    public static List<Producto> parsear(JSONArray arreglo) throws JSONException {
        List<Producto> lista = new ArrayList<>();
        for (int i = 0; i < arreglo.length(); i++) {
            lista.add(parsear(arreglo.getJSONObject(i)));
        }
        return lista;
    }
}
