package cr.ac.monkeystore.movil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.modelo.CompraParser;
import cr.ac.monkeystore.movil.modelo.FiltroCompras;

public class PruebaFiltroYParser {
    private List<Compra> compras;

    @Before
    public void preparar() throws Exception {
        String json = "[{\"id\":1,\"producto\":\"Charizard ex\",\"descripcion\":\"d\",\"precio\":48000,"
                + "\"cantidad\":1,\"fecha\":\"2026-11-10T12:00:00\",\"monto\":48000,\"catalogo\":\"Pokémon TCG\","
                + "\"tipo\":\"Carta suelta\",\"pedido\":\"TMS-000001\",\"estado\":\"Pagado\",\"condicion\":\"Nueva\","
                + "\"cliente\":\"María\",\"reserva\":false},"
                + "{\"id\":2,\"producto\":\"Sobre 25th Anniversary\",\"descripcion\":\"d\",\"precio\":2900,"
                + "\"cantidad\":3,\"fecha\":\"2026-11-11T08:00:00\",\"monto\":8700,\"catalogo\":\"Yu-Gi-Oh!\","
                + "\"tipo\":\"Sobre\",\"pedido\":\"TMS-000002\",\"estado\":\"Enviado\",\"condicion\":\"\","
                + "\"cliente\":\"José\",\"reserva\":true}]";
        compras = CompraParser.parsear(new JSONArray(json));
    }

    @Test
    public void elParserLeeTodosLosCampos() {
        assertEquals(2, compras.size());
        assertEquals("Charizard ex", compras.get(0).getProducto());
        assertEquals(48000.0, compras.get(0).getMonto(), 0.001);
        assertEquals("TMS-000002", compras.get(1).getPedido());
        assertTrue(compras.get(1).esReserva());
    }

    @Test
    public void filtraPorFranquicia() {
        assertEquals(1, FiltroCompras.filtrar(compras, "Yu-Gi-Oh!", "").size());
    }

    @Test
    public void buscaPorNombreSinImportarMayusculas() {
        assertEquals(1, FiltroCompras.filtrar(compras, "", "CHARIZARD").size());
    }

    @Test
    public void sinFiltrosDevuelveTodo() {
        assertEquals(2, FiltroCompras.filtrar(compras, null, null).size());
    }

    @Test
    public void combinaFranquiciaYTexto() {
        assertEquals(0, FiltroCompras.filtrar(compras, "Yu-Gi-Oh!", "charizard").size());
    }
}
