package cr.ac.monkeystore.movil.modelo;

import java.io.Serializable;

/**
 * Producto comprado o reservado: una línea de un pedido de The Monkey Store
 * (sobre, caja, carta suelta, accesorio...). Se serializa para viajar entre pantallas.
 */
public class Compra implements Serializable {
    private static final long serialVersionUID = 2L;

    private final int id;
    private final String producto;
    private final String descripcion;
    private final double precio;
    private final int cantidad;
    private final String fecha;
    private final double monto;
    private final String catalogo;
    private final String tipo;
    private final String pedido;
    private final String estado;
    private final String condicion;
    private final String cliente;
    private final boolean reserva;

    /**
     * @param catalogo  franquicia (catálogo 1)
     * @param tipo      Sobre, Caja, Carta suelta...
     * @param pedido    número de pedido (TMS-000012)
     * @param estado    estado del pedido (Pagado, Enviado...)
     * @param condicion estado de la carta suelta o vacío
     * @param reserva   true si es una preventa
     */
    public Compra(int id, String producto, String descripcion, double precio, int cantidad,
                  String fecha, double monto, String catalogo, String tipo, String pedido,
                  String estado, String condicion, String cliente, boolean reserva) {
        this.id = id;
        this.producto = producto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.monto = monto;
        this.catalogo = catalogo;
        this.tipo = tipo;
        this.pedido = pedido;
        this.estado = estado;
        this.condicion = condicion;
        this.cliente = cliente;
        this.reserva = reserva;
    }

    public int getId() { return id; }
    public String getProducto() { return producto; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public String getFecha() { return fecha; }
    public double getMonto() { return monto; }
    public String getCatalogo() { return catalogo; }
    public String getTipo() { return tipo; }
    public String getPedido() { return pedido; }
    public String getEstado() { return estado; }
    public String getCondicion() { return condicion; }
    public String getCliente() { return cliente; }
    public boolean esReserva() { return reserva; }

    /** Un pedido cancelado o sin pagar no suma a los ingresos. */
    public boolean cuentaComoVenta() {
        return !"Cancelado".equalsIgnoreCase(estado) && !estado.toLowerCase().startsWith("pendiente");
    }
}
