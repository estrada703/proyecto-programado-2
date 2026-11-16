package cr.ac.sabortico.movil.datos.remoto;

import com.android.volley.RequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import cr.ac.sabortico.movil.seguridad.Cifrador;

/** POST /api/movil/ingresar: respuesta { "nombre": "...", "token": "..." }. */
public class PeticionIngresar extends PeticionCifrada {
    public PeticionIngresar(RequestQueue cola, Cifrador cifrador, String urlBase) {
        super(cola, cifrador, urlBase);
    }

    @Override
    protected String ruta() { return "api/movil/ingresar"; }

    @Override
    protected ResultadoAuth interpretar(JSONObject r) throws JSONException {
        return new ResultadoAuth(r.getString("nombre"), r.getString("token"));
    }
}
