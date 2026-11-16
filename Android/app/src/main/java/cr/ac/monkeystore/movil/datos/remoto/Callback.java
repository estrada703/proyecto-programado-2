package cr.ac.monkeystore.movil.datos.remoto;

/** Respuesta asíncrona de la Web API. */
public interface Callback<T> {
    void alExito(T resultado);

    void alError(ErrorApi error);
}
