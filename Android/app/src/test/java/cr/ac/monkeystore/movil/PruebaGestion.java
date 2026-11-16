package cr.ac.monkeystore.movil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import cr.ac.monkeystore.movil.modelo.FiltroInventario;
import cr.ac.monkeystore.movil.modelo.Producto;
import cr.ac.monkeystore.movil.modelo.TransicionesPedido;

public class PruebaGestion {
    private final List<Producto> inventario = Arrays.asList(
            new Producto(1, "Charizard ex", "Pokémon TCG", "Carta suelta", "Nueva", 48000, 0, false),
            new Producto(2, "Sobre 25th", "Yu-Gi-Oh!", "Sobre", "", 2900, 3, false),
            new Producto(3, "Caja Draft", "Magic", "Caja", "", 98000, 10, false));

    @Test
    public void unPedidoPagadoSePuedeEnviarOCancelar() {
        assertEquals(Arrays.asList("Enviado", "Cancelado"), TransicionesPedido.siguientes("Pagado"));
    }

    @Test
    public void unPedidoPendienteSePuedePagar() {
        assertTrue(TransicionesPedido.siguientes("Pendiente de pago").contains("Pagado"));
    }

    @Test
    public void entregadoYCanceladoSonFinales() {
        assertTrue(TransicionesPedido.siguientes("Entregado").isEmpty());
        assertTrue(TransicionesPedido.siguientes("Cancelado").isEmpty());
    }

    @Test
    public void nivelesDeStock() {
        assertEquals(Producto.Nivel.AGOTADO, inventario.get(0).nivel());
        assertEquals(Producto.Nivel.BAJO, inventario.get(1).nivel());
        assertEquals(Producto.Nivel.NORMAL, inventario.get(2).nivel());
    }

    @Test
    public void filtroDeAlertasIncluyeAgotadosYBajos() {
        assertEquals(2, FiltroInventario.filtrar(inventario, "", FiltroInventario.Vista.STOCK_BAJO).size());
        assertEquals(1, FiltroInventario.filtrar(inventario, "", FiltroInventario.Vista.AGOTADOS).size());
        assertEquals(2, FiltroInventario.alertas(inventario));
    }

    @Test
    public void buscaPorNombreOFranquicia() {
        assertEquals(1, FiltroInventario.filtrar(inventario, "magic", FiltroInventario.Vista.TODOS).size());
    }

    @Test
    public void validaRangoDeStock() {
        assertTrue(Producto.stockValido(0));
        assertFalse(Producto.stockValido(-1));
        assertFalse(Producto.stockValido(10000));
    }
}
