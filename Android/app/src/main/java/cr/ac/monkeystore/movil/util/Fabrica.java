package cr.ac.monkeystore.movil.util;

import android.content.Context;

import com.android.volley.RequestQueue;

import cr.ac.monkeystore.movil.BuildConfig;
import cr.ac.monkeystore.movil.datos.local.AdminDbHelper;
import cr.ac.monkeystore.movil.datos.local.CacheListado;
import cr.ac.monkeystore.movil.datos.local.CacheListadoSqlite;
import cr.ac.monkeystore.movil.datos.local.RepositorioCredenciales;
import cr.ac.monkeystore.movil.datos.local.RepositorioCredencialesSqlite;
import cr.ac.monkeystore.movil.datos.remoto.ClienteVolley;
import cr.ac.monkeystore.movil.datos.remoto.PeticionIngresar;
import cr.ac.monkeystore.movil.datos.remoto.PeticionValidar;
import cr.ac.monkeystore.movil.datos.remoto.ServicioAutenticacion;
import cr.ac.monkeystore.movil.datos.remoto.ServicioAutenticacionVolley;
import cr.ac.monkeystore.movil.datos.remoto.ServicioCompras;
import cr.ac.monkeystore.movil.datos.remoto.ServicioComprasVolley;
import cr.ac.monkeystore.movil.datos.remoto.ServicioGestion;
import cr.ac.monkeystore.movil.datos.remoto.ServicioGestionVolley;
import cr.ac.monkeystore.movil.seguridad.Cifrador;
import cr.ac.monkeystore.movil.seguridad.CifradorAesGcm;
import cr.ac.monkeystore.movil.seguridad.CodificadorBase64Android;
import cr.ac.monkeystore.movil.seguridad.HashClave;

/**
 * Ensambla las implementaciones concretas. Las pantallas piden interfaces
 * y nunca crean Volley ni SQLite directamente (inversión de dependencias).
 */
public final class Fabrica {
    private Fabrica() { }

    public static RepositorioCredenciales credenciales(Context c) {
        return new RepositorioCredencialesSqlite(new AdminDbHelper(c), new HashClave());
    }

    public static CacheListado cache(Context c) {
        return new CacheListadoSqlite(new AdminDbHelper(c));
    }

    /** @throws IllegalStateException / IllegalArgumentException si la clave AES falta o es inválida */
    public static ServicioAutenticacion autenticacion(Context c) {
        RequestQueue cola = ClienteVolley.cola(c);
        Cifrador cifrador = cifrador();
        return new ServicioAutenticacionVolley(
                new PeticionValidar(cola, cifrador, BuildConfig.API_BASE_URL),
                new PeticionIngresar(cola, cifrador, BuildConfig.API_BASE_URL));
    }

    public static ServicioCompras compras(Context c) {
        return new ServicioComprasVolley(ClienteVolley.cola(c), BuildConfig.API_BASE_URL);
    }

    public static ServicioGestion gestion(Context c) {
        return new ServicioGestionVolley(ClienteVolley.cola(c), BuildConfig.API_BASE_URL);
    }

    private static Cifrador cifrador() {
        CodificadorBase64Android b64 = new CodificadorBase64Android();
        return new CifradorAesGcm(ClaveAes.obtener(b64), b64);
    }
}
