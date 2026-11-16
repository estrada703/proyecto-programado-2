package cr.ac.monkeystore.movil.datos.remoto;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/** RequestQueue única para toda la aplicación (T5). */
public final class ClienteVolley {
    private static RequestQueue cola;

    private ClienteVolley() { }

    public static synchronized RequestQueue cola(Context contexto) {
        if (cola == null) {
            cola = Volley.newRequestQueue(contexto.getApplicationContext());
        }
        return cola;
    }
}
