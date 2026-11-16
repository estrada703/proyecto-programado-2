package cr.ac.sabortico.movil.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Resumen con sal (PBKDF2) para no guardar la contraseña en texto plano (T4).
 * Se usa PBKDF2WithHmacSHA1 porque está disponible desde API 10 (minSdk 24).
 */
public class HashClave {
    private static final int ITERACIONES = 10000;
    private static final int BITS = 256;
    private static final int BYTES_SAL = 16;

    private final SecureRandom aleatorio = new SecureRandom();

    /** @return sal aleatoria en hexadecimal */
    public String generarSal() {
        byte[] sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        return aHex(sal);
    }

    /** @return resumen PBKDF2 de la contraseña con la sal dada, en hexadecimal */
    public String resumir(String clave, String salHex) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), deHex(salHex), ITERACIONES, BITS);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return aHex(f.generateSecret(spec).getEncoded());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        }
    }

    /** Compara en tiempo constante el resumen guardado con el de la contraseña escrita. */
    public boolean verificar(String clave, String salHex, String hashEsperado) {
        byte[] a = resumir(clave, salHex).getBytes();
        byte[] b = hashEsperado.getBytes();
        return MessageDigest.isEqual(a, b);
    }

    private static String aHex(byte[] datos) {
        StringBuilder sb = new StringBuilder();
        for (byte d : datos) {
            sb.append(String.format("%02x", d));
        }
        return sb.toString();
    }

    private static byte[] deHex(String hex) {
        byte[] r = new byte[hex.length() / 2];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return r;
    }
}
