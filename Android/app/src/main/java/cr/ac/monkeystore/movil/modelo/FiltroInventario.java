package cr.ac.monkeystore.movil.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Filtra el inventario en memoria por nombre y por nivel de stock. */
public final class FiltroInventario {
    public enum Vista { TODOS, STOCK_BAJO, AGOTADOS }

    private FiltroInventario() { }

    public static List<Producto> filtrar(List<Producto> origen, String texto, Vista vista) {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        List<Producto> r = new ArrayList<>();
        for (Producto p : origen) {
            boolean coincideTexto = buscado.isEmpty()
                    || p.getNombre().toLowerCase(Locale.ROOT).contains(buscado)
                    || p.getCatalogo().toLowerCase(Locale.ROOT).contains(buscado);
            boolean coincideVista = vista == Vista.TODOS
                    || (vista == Vista.AGOTADOS && p.nivel() == Producto.Nivel.AGOTADO)
                    || (vista == Vista.STOCK_BAJO && p.nivel() != Producto.Nivel.NORMAL);
            if (coincideTexto && coincideVista) r.add(p);
        }
        return r;
    }

    /** Cuántos productos están agotados o con stock bajo. */
    public static int alertas(List<Producto> origen) {
        int n = 0;
        for (Producto p : origen) if (p.nivel() != Producto.Nivel.NORMAL) n++;
        return n;
    }
}
