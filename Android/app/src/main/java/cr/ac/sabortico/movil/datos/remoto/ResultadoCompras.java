package cr.ac.sabortico.movil.datos.remoto;

import java.util.List;

import cr.ac.sabortico.movil.modelo.Compra;

/** Listado ya convertido, más el JSON original para la caché sin conexión. */
public class ResultadoCompras {
    private final List<Compra> compras;
    private final String json;

    public ResultadoCompras(List<Compra> compras, String json) {
        this.compras = compras;
        this.json = json;
    }

    public List<Compra> getCompras() { return compras; }

    public String getJson() { return json; }
}
