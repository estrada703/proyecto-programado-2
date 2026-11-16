package cr.ac.monkeystore.movil;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cr.ac.monkeystore.movil.seguridad.HashClave;

public class PruebaHashClave {
    private final HashClave hash = new HashClave();

    @Test
    public void laContrasenaCorrectaSeVerifica() {
        String sal = hash.generarSal();
        String resumen = hash.resumir("Admin123!", sal);
        assertTrue(hash.verificar("Admin123!", sal, resumen));
    }

    @Test
    public void laContrasenaIncorrectaSeRechaza() {
        String sal = hash.generarSal();
        assertFalse(hash.verificar("otra", sal, hash.resumir("Admin123!", sal)));
    }

    @Test
    public void elResumenNoEsLaContrasenaEnTextoPlano() {
        String sal = hash.generarSal();
        String resumen = hash.resumir("Admin123!", sal);
        assertNotNull(resumen);
        assertNotEquals("Admin123!", resumen);
    }
}
