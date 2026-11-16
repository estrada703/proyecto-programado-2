package cr.ac.sabortico.movil.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cr.ac.sabortico.movil.R;
import cr.ac.sabortico.movil.modelo.Compra;
import cr.ac.sabortico.movil.modelo.Sesion;
import cr.ac.sabortico.movil.util.Bitacora;
import cr.ac.sabortico.movil.util.Fabrica;

/** Pantalla principal: aloja los fragments de listado, detalle y perfil. */
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_NOMBRE = "cr.ac.sabortico.movil.NOMBRE";
    public static final String EXTRA_CORREO = "cr.ac.sabortico.movil.CORREO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Sesion.obtener().estaActiva()) {
            irAlIngreso();
            return;
        }
        setContentView(R.layout.activity_main);

        final String nombre = getIntent().getStringExtra(EXTRA_NOMBRE);
        final String correo = getIntent().getStringExtra(EXTRA_CORREO);

        BottomNavigationView nav = findViewById(R.id.navegacion);
        nav.setOnItemSelectedListener(item -> {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Fragment f = item.getItemId() == R.id.menu_perfil
                    ? PerfilFragment.nuevo(nombre, correo)
                    : new ListadoFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, f).commit();
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.menu_listado);
            Toast.makeText(this, getString(R.string.msg_ingreso_ok, nombre), Toast.LENGTH_SHORT).show();
        }
    }

    /** Abre el detalle del producto elegido pasándole los datos en un Bundle (T3). */
    public void abrirDetalle(Compra compra) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contenedor, DetalleFragment.nuevo(compra))
                .addToBackStack(null)
                .commit();
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
