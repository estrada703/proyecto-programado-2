package cr.ac.monkeystore.movil.seguridad;

import java.security.GeneralSecurityException;

/** Contrato para proteger un texto antes de enviarlo por la red. */
public interface Cifrador {
    /** @return el mensaje cifrado listo para viajar en el campo "datos". */
    String cifrar(String textoPlano) throws GeneralSecurityException;

    /** @return el texto original; lanza excepción si el mensaje fue alterado. */
    String descifrar(String datos) throws GeneralSecurityException;
}
