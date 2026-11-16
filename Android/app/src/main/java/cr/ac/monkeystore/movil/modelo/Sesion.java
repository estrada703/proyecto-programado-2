package cr.ac.monkeystore.movil.modelo;

import java.util.List;

/**
 * Sesión del administrador. Vive solo en memoria: al cerrar la aplicación
 * se pierde y se vuelve a pedir el ingreso (F6).
 */
public final class Sesion {
    private static final Sesion INSTANCIA = new Sesion();

    private String token;
    private String nombre;
    private String correo;
    private List<Compra> compras;
    private String filtroTexto = "";
    private String filtroCatalogo = "";
    private long fechaCache;

    private Sesion() { }

    public static Sesion obtener() { return INSTANCIA; }

    public void iniciar(String token, String nombre, String correo) {
        this.token = token;
        this.nombre = nombre;
        this.correo = correo;
        this.compras = null;
        this.filtroTexto = "";
        this.filtroCatalogo = "";
    }

    public void cerrar() {
        token = null;
        nombre = null;
        correo = null;
        compras = null;
        filtroTexto = "";
        filtroCatalogo = "";
        fechaCache = 0;
    }

    public boolean estaActiva() { return token != null; }
    public String getToken() { return token; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public List<Compra> getCompras() { return compras; }
    public void setCompras(List<Compra> compras) { this.compras = compras; }
    public String getFiltroTexto() { return filtroTexto; }
    public void setFiltroTexto(String filtroTexto) { this.filtroTexto = filtroTexto; }
    /** @return marca de tiempo de la caché mostrada, o 0 si los datos vienen en línea (E1). */
    public long getFechaCache() { return fechaCache; }
    public void setFechaCache(long fechaCache) { this.fechaCache = fechaCache; }
    public String getFiltroCatalogo() { return filtroCatalogo; }
    public void setFiltroCatalogo(String filtroCatalogo) { this.filtroCatalogo = filtroCatalogo; }
}
