package cr.ac.sabortico.movil.datos.local;

/** Almacén de los administradores registrados en este dispositivo (F2). */
public interface RepositorioCredenciales {
    enum ResultadoRegistro { REGISTRADO, YA_EXISTE, ERROR }

    ResultadoRegistro registrar(String nombre, String correo, String clave);

    boolean existe(String correo);

    /** @return true si la contraseña coincide con la registrada en el dispositivo. */
    boolean verificar(String correo, String clave);

    String nombrePorCorreo(String correo);
}
