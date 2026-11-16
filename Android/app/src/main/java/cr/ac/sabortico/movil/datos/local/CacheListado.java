package cr.ac.sabortico.movil.datos.local;

/** Guarda el último listado recibido para mostrarlo sin conexión (E1). */
public interface CacheListado {
    void guardar(String json);

    /** @return el último JSON guardado o null si no hay. */
    String leerJson();

    /** @return marca de tiempo (ms) de la última actualización, 0 si no hay. */
    long leerFecha();

    void limpiar();
}
