package cr.ac.sabortico.movil.seguridad;

/** Abstrae Base64 para poder probar el cifrado fuera de Android (principio D de SOLID). */
public interface Codificador64 {
    String codificar(byte[] datos);

    byte[] decodificar(String texto);
}
