package cr.ac.monkeystore.movil.datos.remoto;

/** Error de la Web API ya clasificado para que la pantalla muestre un mensaje distinto por caso. */
public class ErrorApi {
    public enum Tipo {
        PETICION_INVALIDA,   // 400
        NO_AUTORIZADO,       // 401
        PROHIBIDO,           // 403
        SERVIDOR,            // 5xx
        RED,                 // sin conexión
        TIEMPO_AGOTADO,
        RESPUESTA_INVALIDA,  // JSON mal formado
        CIFRADO,
        DESCONOCIDO
    }

    private final Tipo tipo;

    public ErrorApi(Tipo tipo) { this.tipo = tipo; }

    public Tipo getTipo() { return tipo; }
}
