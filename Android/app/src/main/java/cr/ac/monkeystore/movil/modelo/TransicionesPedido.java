package cr.ac.monkeystore.movil.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Cambios de estado que el administrador puede hacer desde la app.
 * Es la misma regla que valida la API (409 si no se permite).
 */
public final class TransicionesPedido {
    /** Códigos que entiende la API en PUT /api/movil/pedidos/{id}/estado. */
    public static final String PAGADO = "Pagado";
    public static final String ENVIADO = "Enviado";
    public static final String ENTREGADO = "Entregado";
    public static final String CANCELADO = "Cancelado";

    private TransicionesPedido() { }

    /** @param estadoActual nombre que muestra la API ("Pendiente de pago", "Pagado"...) */
    public static List<String> siguientes(String estadoActual) {
        String e = estadoActual == null ? "" : estadoActual.toLowerCase(Locale.ROOT);
        List<String> r = new ArrayList<>();
        if (e.startsWith("pendiente")) {
            r.add(PAGADO);
            r.add(CANCELADO);
        } else if (e.startsWith("pagado")) {
            r.add(ENVIADO);
            r.add(CANCELADO);
        } else if (e.startsWith("enviado")) {
            r.add(ENTREGADO);
        }
        return Collections.unmodifiableList(r);
    }
}
