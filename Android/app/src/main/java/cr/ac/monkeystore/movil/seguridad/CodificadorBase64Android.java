package cr.ac.monkeystore.movil.seguridad;

import android.util.Base64;

/** Implementación para el teléfono con android.util.Base64 (java.util.Base64 exige API 26). */
public class CodificadorBase64Android implements Codificador64 {
    @Override
    public String codificar(byte[] datos) {
        return Base64.encodeToString(datos, Base64.NO_WRAP);
    }

    @Override
    public byte[] decodificar(String texto) {
        return Base64.decode(texto, Base64.NO_WRAP);
    }
}
