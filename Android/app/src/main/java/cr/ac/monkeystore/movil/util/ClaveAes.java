package cr.ac.monkeystore.movil.util;

import cr.ac.monkeystore.movil.BuildConfig;
import cr.ac.monkeystore.movil.seguridad.Codificador64;

/** Lee la clave AES de BuildConfig (viene de local.properties, no del código). */
public final class ClaveAes {
    private ClaveAes() { }

    /** @throws IllegalStateException si local.properties no define aes.key.base64 */
    public static byte[] obtener(Codificador64 codificador) {
        if (BuildConfig.AES_KEY_BASE64 == null || BuildConfig.AES_KEY_BASE64.isEmpty()) {
            throw new IllegalStateException("aes.key.base64 no configurada");
        }
        return codificador.decodificar(BuildConfig.AES_KEY_BASE64);
    }
}
