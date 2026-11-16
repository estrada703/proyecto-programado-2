package cr.ac.sabortico.movil.datos.remoto;

/** Lo que la pantalla necesita de la API para registrar e ingresar, sin saber que detrás hay Volley. */
public interface ServicioAutenticacion {
    /** POST /api/movil/validar: ¿es un administrador válido? */
    void validar(String correo, String clave, Callback<ResultadoAuth> callback);

    /** POST /api/movil/ingresar: devuelve nombre y token de sesión. */
    void ingresar(String correo, String clave, Callback<ResultadoAuth> callback);
}
