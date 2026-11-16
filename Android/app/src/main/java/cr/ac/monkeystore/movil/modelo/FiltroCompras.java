package cr.ac.monkeystore.movil.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Filtra en memoria por catálogo 1 (franquicia) y por nombre del producto (F5). */
public final class FiltroCompras {

    private FiltroCompras() { }

    /**
     * @param origen   lista completa ya descargada
     * @param catalogo franquicia elegida; null o vacío significa todas
     * @param texto    texto a buscar en el nombre del producto; null o vacío significa todo
     */
    public static List<Compra> filtrar(List<Compra> origen, String catalogo, String texto) {
        List<Compra> resultado = new ArrayList<>();
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        for (Compra c : origen) {
            boolean coincideCatalogo = catalogo == null || catalogo.isEmpty()
                    || catalogo.equals(c.getCatalogo());
            boolean coincideTexto = buscado.isEmpty()
                    || c.getProducto().toLowerCase(Locale.ROOT).contains(buscado);
            if (coincideCatalogo && coincideTexto) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
