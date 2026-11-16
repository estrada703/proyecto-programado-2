package cr.ac.sabortico.movil.datos.remoto;

/** Consulta del listado de compras y reservas. */
public interface ServicioCompras {
    /** GET /api/movil/compras con Authorization: Bearer token. */
    void obtenerCompras(String token, Callback<ResultadoCompras> callback);
}
