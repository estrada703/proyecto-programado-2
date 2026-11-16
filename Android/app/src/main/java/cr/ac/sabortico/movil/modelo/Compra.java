package cr.ac.sabortico.movil.modelo;

import java.io.Serializable;

/**
 * Producto comprado o reservado en el negocio (platillo, entrada, cupo...).
 * Se serializa para viajar entre pantallas dentro de un Bundle.
 */
public class Compra implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String producto;
    private final String descripcion;
    private final double precio;
    private final int cantidad;
    private final String fecha;
    private final double monto;
    private final String catalogo;
    private final String tipo;

    public Compra(int id, String producto, String descripcion, double precio, int cantidad,
                  String fecha, double monto, String catalogo, String tipo) {
        this.id = id;
        this.producto = producto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.monto = monto;
        this.catalogo = catalogo;
        this.tipo = tipo;
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
}
