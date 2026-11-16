package cr.ac.sabortico.movil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.util.Base64;

import cr.ac.sabortico.movil.seguridad.CifradorAesGcm;
import cr.ac.sabortico.movil.seguridad.Codificador64;

public class PruebaCifradorAesGcm {
    private final Codificador64 b64 = new Codificador64() {
        @Override public String codificar(byte[] d) { return Base64.getEncoder().encodeToString(d); }
        @Override public byte[] decodificar(String t) { return Base64.getDecoder().decode(t); }
    };
    private CifradorAesGcm cifrador;

    @Before
    public void preparar() {
        cifrador = new CifradorAesGcm("0123456789abcdef0123456789abcdef".getBytes(), b64);
    }

    @Test
    public void cifrarYDescifrarDevuelveElTextoOriginal() throws Exception {
        String json = "{\"correo\":\"admin@negocio.cr\",\"clave\":\"Admin123!\"}";
        assertEquals(json, cifrador.descifrar(cifrador.cifrar(json)));
    }

    @Test
    public void cadaMensajeUsaUnIvDistinto() throws Exception {
        assertNotEquals(cifrador.cifrar("hola"), cifrador.cifrar("hola"));
    }

    @Test
    public void elFormatoEsIv12MasCifradoMasEtiqueta16() throws Exception {
        byte[] paquete = b64.decodificar(cifrador.cifrar("abc"));
        assertEquals(12 + 3 + 16, paquete.length);
    }

    @Test
    public void unMensajeAlteradoLanzaExcepcion() throws Exception {
        byte[] paquete = b64.decodificar(cifrador.cifrar("texto secreto"));
        paquete[paquete.length - 1] ^= 1; // se altera la etiqueta
        try {
            cifrador.descifrar(b64.codificar(paquete));
            org.junit.Assert.fail("Debió lanzar excepción");
        } catch (GeneralSecurityException esperado) {
            assertTrue(true);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unaClaveDeLongitudIncorrectaSeRechaza() {
        new CifradorAesGcm(new byte[16], b64);
    }
}
