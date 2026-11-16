package cr.ac.monkeystore.movil.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Indicadores del panel de resumen, calculados en memoria a partir del listado
 * ya descargado (no hace otra petición a la API).
 */
public class Resumen {

    /** Par nombre-valor para gráficos y rankings. */
    public static class Item {
        private final String nombre;
        private final double valor;

        public Item(String nombre, double valor) {
            this.nombre = nombre;
            this.valor = valor;
        }

        public String getNombre() { return nombre; }
        public double getValor() { return valor; }
    }

    private final double ingresos;
    private final int pedidos;
    private final int articulos;
    private final int reservas;
    private final List<Item> porFranquicia;
    private final List<Item> masVendidos;
    private final Map<String, Integer> porEstado;

    private Resumen(double ingresos, int pedidos, int articulos, int reservas,
                    List<Item> porFranquicia, List<Item> masVendidos, Map<String, Integer> porEstado) {
        this.ingresos = ingresos;
        this.pedidos = pedidos;
        this.articulos = articulos;
        this.reservas = reservas;
        this.porFranquicia = porFranquicia;
        this.masVendidos = masVendidos;
        this.porEstado = porEstado;
    }

    /**
     * @param compras listado completo
     * @param top     cuántos productos incluir en "más vendidos"
     */
    public static Resumen calcular(List<Compra> compras, int top) {
        double ingresos = 0;
        int articulos = 0;
        int reservas = 0;
        Set<String> pedidosVenta = new HashSet<>();
        Map<String, Double> franquicias = new LinkedHashMap<>();
        Map<String, Double> productos = new LinkedHashMap<>();
        Map<String, Set<String>> pedidosPorEstado = new LinkedHashMap<>();

        for (Compra c : compras) {
            Set<String> enEstado = pedidosPorEstado.get(c.getEstado());
            if (enEstado == null) {
                enEstado = new HashSet<>();
                pedidosPorEstado.put(c.getEstado(), enEstado);
            }
            enEstado.add(c.getPedido());

            if (!c.cuentaComoVenta()) {
                continue;
            }
            ingresos += c.getMonto();
            articulos += c.getCantidad();
            if (c.esReserva()) {
                reservas += c.getCantidad();
            }
            pedidosVenta.add(c.getPedido());
            sumar(franquicias, c.getCatalogo(), c.getMonto());
            sumar(productos, c.getProducto(), c.getCantidad());
        }

        Map<String, Integer> porEstado = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : pedidosPorEstado.entrySet()) {
            porEstado.put(e.getKey(), e.getValue().size());
        }
        return new Resumen(ingresos, pedidosVenta.size(), articulos, reservas,
                ordenar(franquicias, Integer.MAX_VALUE), ordenar(productos, top), porEstado);
    }

    private static void sumar(Map<String, Double> mapa, String clave, double valor) {
        Double actual = mapa.get(clave);
        mapa.put(clave, (actual == null ? 0 : actual) + valor);
    }

    private static List<Item> ordenar(Map<String, Double> mapa, int limite) {
        List<Item> lista = new ArrayList<>();
        for (Map.Entry<String, Double> e : mapa.entrySet()) {
            lista.add(new Item(e.getKey(), e.getValue()));
        }
        Collections.sort(lista, (a, b) -> Double.compare(b.getValor(), a.getValor()));
        return lista.size() > limite ? new ArrayList<>(lista.subList(0, limite)) : lista;
    }

    public double getIngresos() { return ingresos; }
    public int getPedidos() { return pedidos; }
    public int getArticulos() { return articulos; }
    public int getReservas() { return reservas; }

    /** Ingreso promedio por pedido vendido (0 si no hay ventas). */
    public double getTicketPromedio() { return pedidos == 0 ? 0 : ingresos / pedidos; }

    public List<Item> getPorFranquicia() { return porFranquicia; }
    public List<Item> getMasVendidos() { return masVendidos; }
    public Map<String, Integer> getPorEstado() { return porEstado; }
}
