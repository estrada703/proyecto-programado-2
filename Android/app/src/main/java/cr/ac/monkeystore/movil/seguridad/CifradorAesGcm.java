package cr.ac.monkeystore.movil.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cifrado AES-256-GCM. El mensaje que viaja es
 * Base64( IV(12) || texto cifrado || etiqueta(16) ).
 * Se genera un IV aleatorio nuevo por cada mensaje.
 */
public class CifradorAesGcm implements Cifrador {
    public static final int LONGITUD_CLAVE = 32;
    public static final int LONGITUD_IV = 12;
    public static final int LONGITUD_ETIQUETA = 16;

    private final SecretKeySpec clave;
    private final Codificador64 codificador;
    private final SecureRandom aleatorio = new SecureRandom();

    /**
     * @param clave       exactamente 32 bytes (AES-256)
     * @param codificador implementación de Base64
     */
    public CifradorAesGcm(byte[] clave, Codificador64 codificador) {
        if (clave == null || clave.length != LONGITUD_CLAVE) {
            throw new IllegalArgumentException("La clave AES debe tener 32 bytes.");
        }
        this.clave = new SecretKeySpec(clave, "AES");
        this.codificador = codificador;
    }

    @Override
    public String cifrar(String textoPlano) throws GeneralSecurityException {
        byte[] iv = new byte[LONGITUD_IV];
        aleatorio.nextBytes(iv); // un IV nuevo por mensaje
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(LONGITUD_ETIQUETA * 8, iv));
        byte[] cifradoConEtiqueta = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
        byte[] salida = new byte[iv.length + cifradoConEtiqueta.length];
        System.arraycopy(iv, 0, salida, 0, iv.length);
        System.arraycopy(cifradoConEtiqueta, 0, salida, iv.length, cifradoConEtiqueta.length);
        return codificador.codificar(salida);
    }

    @Override
    public String descifrar(String datos) throws GeneralSecurityException {
        byte[] paquete = codificador.decodificar(datos);
        if (paquete.length < LONGITUD_IV + LONGITUD_ETIQUETA) {
            throw new GeneralSecurityException("Mensaje demasiado corto.");
        }
        byte[] iv = new byte[LONGITUD_IV];
        System.arraycopy(paquete, 0, iv, 0, LONGITUD_IV);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(LONGITUD_ETIQUETA * 8, iv));
        byte[] plano = cipher.doFinal(paquete, LONGITUD_IV, paquete.length - LONGITUD_IV);
        return new String(plano, StandardCharsets.UTF_8);
    }
}
