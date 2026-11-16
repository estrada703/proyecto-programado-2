package cr.ac.monkeystore.movil;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.modelo.Resumen;

public class PruebaResumen {
    private static Compra c(String pedido, String producto, String franquicia, int cant, double monto,
                            String estado, boolean reserva) {
        return new Compra(1, producto, "", monto / cant, cant, "2026-11-01T10:00:00", monto,
                franquicia, "Sobre", pedido, estado, "", "Cliente", reserva);
    }

    private final List<Compra> datos = Arrays.asList(
            c("TMS-1", "Sobre A", "Pokémon TCG", 2, 6000, "Pagado", false),
            c("TMS-1", "Caja B", "Magic", 1, 50000, "Pagado", true),
            c("TMS-2", "Sobre A", "Pokémon TCG", 3, 9000, "Entregado", false),
            c("TMS-3", "Caja B", "Magic", 1, 50000, "Cancelado", false));

    @Test
    public void losCanceladosNoSumanIngresos() {
        Resumen r = Resumen.calcular(datos, 5);
        assertEquals(65000, r.getIngresos(), 0.001);
        assertEquals(2, r.getPedidos());
        assertEquals(6, r.getArticulos());
        assertEquals(1, r.getReservas());
    }

    @Test
    public void ticketPromedioEsIngresosEntrePedidos() {
        assertEquals(32500, Resumen.calcular(datos, 5).getTicketPromedio(), 0.001);
    }

    @Test
    public void franquiciasOrdenadasDeMayorAMenor() {
        Resumen r = Resumen.calcular(datos, 5);
        assertEquals("Magic", r.getPorFranquicia().get(0).getNombre());
        assertEquals(50000, r.getPorFranquicia().get(0).getValor(), 0.001);
    }

    @Test
    public void masVendidosPorUnidadesYConLimite() {
        Resumen r = Resumen.calcular(datos, 1);
        assertEquals(1, r.getMasVendidos().size());
        assertEquals("Sobre A", r.getMasVendidos().get(0).getNombre());
    }

    @Test
    public void cuentaPedidosPorEstado() {
        Resumen r = Resumen.calcular(datos, 5);
        assertEquals(Integer.valueOf(1), r.getPorEstado().get("Cancelado"));
        assertEquals(Integer.valueOf(1), r.getPorEstado().get("Pagado"));
    }

    @Test
    public void listaVaciaNoFalla() {
        Resumen r = Resumen.calcular(Collections.<Compra>emptyList(), 5);
        assertEquals(0, r.getTicketPromedio(), 0.001);
    }
}
