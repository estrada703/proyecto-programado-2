package cr.ac.monkeystore.movil.datos.remoto;

/** Datos devueltos por la API al validar o ingresar. */
public class ResultadoAuth {
    private final String nombre;
    private final String token;

    public ResultadoAuth(String nombre, String token) {
        this.nombre = nombre;
        this.token = token;
    }

    public String getNombre() { return nombre; }

    public String getToken() { return token; }
}
