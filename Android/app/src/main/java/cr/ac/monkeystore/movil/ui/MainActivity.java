package cr.ac.monkeystore.movil.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;

import java.util.List;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.local.CacheListado;
import cr.ac.monkeystore.movil.datos.remoto.Callback;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;
import cr.ac.monkeystore.movil.datos.remoto.ResultadoCompras;
import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.modelo.CompraParser;
import cr.ac.monkeystore.movil.modelo.Sesion;
import cr.ac.monkeystore.movil.util.Bitacora;
import cr.ac.monkeystore.movil.util.Bordes;
import cr.ac.monkeystore.movil.util.Fabrica;

/**
 * Pantalla principal: aloja los fragments de resumen, listado, detalle y perfil.
 * En horizontal o en tableta muestra listado y detalle lado a lado (E2).
 */
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_NOMBRE = "cr.ac.monkeystore.movil.NOMBRE";
    public static final String EXTRA_CORREO = "cr.ac.monkeystore.movil.CORREO";
    private static final String ESTADO_SELECCION = "seleccion";

    /** Lo que reciben los fragments al pedir el listado. */
    public interface EscuchaCompras {
        void alCargar(List<Compra> compras, long fechaCache);

        void alFallar(ErrorApi error);
    }

    private Compra seleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Sesion.obtener().estaActiva()) {
            irAlIngreso();
            return;
        }
        setContentView(R.layout.activity_main);
        Bordes.aplicar(findViewById(android.R.id.content), false);

        BottomNavigationView nav = findViewById(R.id.navegacion);
        nav.setOnItemSelectedListener(item -> {
            mostrarPestana(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.menu_resumen);
            Toast.makeText(this, getString(R.string.msg_ingreso_ok, Sesion.obtener().getNombre()),
                    Toast.LENGTH_SHORT).show();
        } else {
            seleccionada = (Compra) savedInstanceState.getSerializable(ESTADO_SELECCION);
            actualizarPanelDetalle(nav.getSelectedItemId());
            if (seleccionada != null && esDosPaneles()) {
                // Venía de vertical con el detalle a pantalla completa: se pasa al panel derecho.
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                abrirDetalle(seleccionada);
            }
        }
    }

    private void mostrarPestana(int id) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Fragment f;
        if (id == R.id.menu_perfil) {
            f = PerfilFragment.nuevo(getIntent().getStringExtra(EXTRA_NOMBRE),
                    getIntent().getStringExtra(EXTRA_CORREO));
        } else if (id == R.id.menu_inventario) {
            f = new InventarioFragment();
        } else if (id == R.id.menu_listado) {
            f = new ListadoFragment();
        } else {
            f = new ResumenFragment();
        }
        fm.beginTransaction().replace(R.id.contenedor, f).commit();
        quitarDetalleDelPanel();
        seleccionada = null;
        actualizarPanelDetalle(id);
    }

    /** El panel derecho solo tiene sentido junto al listado. */
    private void actualizarPanelDetalle(int pestana) {
        View panel = findViewById(R.id.contenedor_detalle);
        View divisor = findViewById(R.id.divisor);
        if (panel != null) {
            boolean visible = pestana == R.id.menu_listado;
            panel.setVisibility(visible ? View.VISIBLE : View.GONE);
            divisor.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public boolean esDosPaneles() {
        return findViewById(R.id.contenedor_detalle) != null;
    }

    /** Abre el detalle pasándole los datos en un Bundle (T3): al lado o a pantalla completa. */
    public void abrirDetalle(Compra compra) {
        seleccionada = compra;
        if (esDosPaneles()) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.contenedor_detalle, DetalleFragment.nuevo(compra))
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.contenedor, DetalleFragment.nuevo(compra))
                    .addToBackStack(null)
                    .commit();
        }
    }

    /** Tras cambiar el estado de un pedido, el listado visible se vuelve a pintar. */
    public void alCambiarPedido() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.contenedor);
        if (f instanceof ListadoFragment) {
            ((ListadoFragment) f).refrescar();
        }
    }

    private void quitarDetalleDelPanel() {
        Fragment d = getSupportFragmentManager().findFragmentById(R.id.contenedor_detalle);
        if (d != null) {
            getSupportFragmentManager().beginTransaction().remove(d).commitNowAllowingStateLoss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle salida) {
        // El panel derecho no existe en vertical: se quita antes de guardar para no restaurarlo ahí.
        quitarDetalleDelPanel();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && !esDosPaneles()) {
            seleccionada = null;
        }
        salida.putSerializable(ESTADO_SELECCION, seleccionada);
        super.onSaveInstanceState(salida);
    }

    /**
     * Obtiene el listado: de memoria si ya está, de la API si se fuerza o no hay,
     * y de la caché SQLite si no hay conexión (E1).
     */
    public void cargarCompras(boolean forzar, final EscuchaCompras escucha) {
        final Sesion sesion = Sesion.obtener();
        if (!forzar && sesion.getCompras() != null) {
            escucha.alCargar(sesion.getCompras(), sesion.getFechaCache());
            return;
        }
        Bitacora.i(Bitacora.TAG_LISTADO, "Cargando listado de compras");
        Fabrica.compras(this).obtenerCompras(sesion.getToken(), new Callback<ResultadoCompras>() {
            @Override
            public void alExito(ResultadoCompras r) {
                if (isFinishing()) return;
                sesion.setCompras(r.getCompras());
                sesion.setFechaCache(0);
                Fabrica.cache(MainActivity.this).guardar(r.getJson());
                escucha.alCargar(r.getCompras(), 0);
            }

            @Override
            public void alError(ErrorApi error) {
                if (isFinishing()) return;
                if (error.getTipo() == ErrorApi.Tipo.NO_AUTORIZADO) {
                    Toast.makeText(MainActivity.this, R.string.api_401_sesion, Toast.LENGTH_LONG).show();
                    cerrarSesion();
                    return;
                }
                boolean sinConexion = error.getTipo() == ErrorApi.Tipo.RED
                        || error.getTipo() == ErrorApi.Tipo.TIEMPO_AGOTADO
                        || error.getTipo() == ErrorApi.Tipo.SERVIDOR;
                if (sinConexion && desdeCache()) {
                    escucha.alCargar(sesion.getCompras(), sesion.getFechaCache());
                    return;
                }
                escucha.alFallar(error);
            }
        });
    }

    private boolean desdeCache() {
        CacheListado cache = Fabrica.cache(this);
        String json = cache.leerJson();
        if (json == null) {
            return false;
        }
        try {
            Sesion.obtener().setCompras(CompraParser.parsear(json));
            Sesion.obtener().setFechaCache(cache.leerFecha());
            Bitacora.w(Bitacora.TAG_LISTADO, "Sin conexión: se muestra la caché");
            return true;
        } catch (JSONException e) {
            Bitacora.e(Bitacora.TAG_LISTADO, "La caché del listado está dañada", e);
            return false;
        }
    }

    public void cerrarSesion() {
        Bitacora.i(Bitacora.TAG_INGRESO, "Sesión cerrada");
        Fabrica.cache(this).limpiar();
        Sesion.obtener().cerrar();
        irAlIngreso();
    }

    private void irAlIngreso() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent); // CLEAR_TASK: "Atrás" ya no regresa al listado
        finish();
    }
}
