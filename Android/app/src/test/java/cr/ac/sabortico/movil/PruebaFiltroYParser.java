package cr.ac.sabortico.movil;

import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import cr.ac.sabortico.movil.modelo.Compra;
import cr.ac.sabortico.movil.modelo.CompraParser;
import cr.ac.sabortico.movil.modelo.FiltroCompras;

public class PruebaFiltroYParser {
    private List<Compra> compras;

    @Before
    public void preparar() throws Exception {
        String json = "[{\"id\":1,\"producto\":\"Casado con pollo\",\"descripcion\":\"d\",\"precio\":3500,"
                + "\"cantidad\":2,\"fecha\":\"2026-11-10T12:00:00\",\"monto\":7000,\"catalogo\":\"Sede San José\",\"tipo\":\"Compra\"},"
                + "{\"id\":2,\"producto\":\"Gallo pinto\",\"descripcion\":\"d\",\"precio\":1800,"
                + "\"cantidad\":1,\"fecha\":\"2026-11-11T08:00:00\",\"monto\":1800,\"catalogo\":\"Sede Cartago\",\"tipo\":\"Reserva\"}]";
        compras = CompraParser.parsear(new JSONArray(json));
    }

    @Test
    public void elParserLeeTodosLosCampos() {
        assertEquals(2, compras.size());
        assertEquals("Casado con pollo", compras.get(0).getProducto());
        assertEquals(7000.0, compras.get(0).getMonto(), 0.001);
        assertEquals("Reserva", compras.get(1).getTipo());
    }

    @Test
    public void filtraPorCatalogo() {
        assertEquals(1, FiltroCompras.filtrar(compras, "Sede Cartago", "").size());
    }

    @Test
    public void buscaPorNombreSinImportarMayusculas() {
        assertEquals(1, FiltroCompras.filtrar(compras, "", "CASADO").size());
    }

    @Test
    public void sinFiltrosDevuelveTodo() {
        assertEquals(2, FiltroCompras.filtrar(compras, null, null).size());
    }

    @Test
    public void combinaCatalogoYTexto() {
        assertEquals(0, FiltroCompras.filtrar(compras, "Sede Cartago", "casado").size());
    }
}
