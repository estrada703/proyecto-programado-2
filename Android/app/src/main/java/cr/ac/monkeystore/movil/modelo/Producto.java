package cr.ac.monkeystore.movil.modelo;

/** Producto del inventario de The Monkey Store. */
public class Producto {
    /** A partir de esta cantidad (inclusive) el stock se considera bajo. */
    public static final int UMBRAL_STOCK_BAJO = 3;
    public static final int STOCK_MAXIMO = 9999;

    public enum Nivel { AGOTADO, BAJO, NORMAL }

    private final int id;
    private final String nombre;
    private final String catalogo;
    private final String tipo;
    private final String condicion;
    private final double precio;
    private final int stock;
    private final boolean preventa;

    public Producto(int id, String nombre, String catalogo, String tipo, String condicion,
                    double precio, int stock, boolean preventa) {
        this.id = id;
        this.nombre = nombre;
        this.catalogo = catalogo;
        this.tipo = tipo;
        this.condicion = condicion;
        this.precio = precio;
        this.stock = stock;
        this.preventa = preventa;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCatalogo() { return catalogo; }
    public String getTipo() { return tipo; }
    public String getCondicion() { return condicion; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public boolean esPreventa() { return preventa; }

    public Nivel nivel() {
        if (stock <= 0) return Nivel.AGOTADO;
        if (stock <= UMBRAL_STOCK_BAJO) return Nivel.BAJO;
        return Nivel.NORMAL;
    }

    /** @return true si el valor se puede guardar como stock. */
    public static boolean stockValido(int valor) {
        return valor >= 0 && valor <= STOCK_MAXIMO;
    }
}
