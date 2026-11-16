package cr.ac.monkeystore.movil.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.local.RepositorioCredenciales;
import cr.ac.monkeystore.movil.datos.remoto.Callback;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;
import cr.ac.monkeystore.movil.datos.remoto.ResultadoAuth;
import cr.ac.monkeystore.movil.datos.remoto.ServicioAutenticacion;
import cr.ac.monkeystore.movil.util.Bitacora;
import cr.ac.monkeystore.movil.util.Bordes;
import cr.ac.monkeystore.movil.util.Fabrica;
import cr.ac.monkeystore.movil.util.Mensajes;

/**
 * Registro del administrador en el dispositivo (F2): la API confirma que es un
 * administrador válido y solo entonces se guarda en SQLite.
 */
public class RegistroActivity extends AppCompatActivity {
    private TextInputLayout tilNombre;
    private TextInputLayout tilCorreo;
    private TextInputLayout tilClave;
    private EditText etNombre;
    private EditText etCorreo;
    private EditText etClave;
    private Button btnRegistrar;
    private Button btnVolver;
    private View progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        Bordes.aplicar(findViewById(android.R.id.content), true);
        etNombre = findViewById(R.id.etNombre);
        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilClave = findViewById(R.id.tilClave);
        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVolver = findViewById(R.id.btnVolver);
        progreso = findViewById(R.id.progreso);

        btnRegistrar.setOnClickListener(v -> intentarRegistro());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void intentarRegistro() {
        final String nombre = etNombre.getText().toString().trim();
        final String correo = etCorreo.getText().toString().trim();
        final String clave = etClave.getText().toString();
        if (!validar(nombre, correo, clave)) {
            return;
        }

        final RepositorioCredenciales repo = Fabrica.credenciales(this);
        if (repo.existe(correo)) {
            Toast.makeText(this, R.string.msg_ya_registrado, Toast.LENGTH_LONG).show();
            return;
        }

        ServicioAutenticacion servicio;
        try {
            servicio = Fabrica.autenticacion(this);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Bitacora.e(Bitacora.TAG_CIFRADO, "Clave AES no disponible", e);
            Toast.makeText(this, R.string.msg_clave_cifrado_faltante, Toast.LENGTH_LONG).show();
            return;
        }

        enCarga(true);
        servicio.validar(correo, clave, new Callback<ResultadoAuth>() {
            @Override
            public void alExito(ResultadoAuth r) {
                enCarga(false);
                RepositorioCredenciales.ResultadoRegistro res = repo.registrar(nombre, correo, clave);
                switch (res) {
                    case REGISTRADO:
                        Bitacora.i(Bitacora.TAG_REGISTRO, "Registro aceptado");
                        Toast.makeText(RegistroActivity.this, R.string.msg_registro_ok, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    case YA_EXISTE:
                        Toast.makeText(RegistroActivity.this, R.string.msg_ya_registrado, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Bitacora.w(Bitacora.TAG_REGISTRO, "Registro rechazado: error de base de datos");
                        Toast.makeText(RegistroActivity.this, R.string.msg_error_bd, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void alError(ErrorApi error) {
                enCarga(false);
                Bitacora.w(Bitacora.TAG_REGISTRO, "Registro rechazado: " + error.getTipo());
                Toast.makeText(RegistroActivity.this,
                        Mensajes.paraError(RegistroActivity.this, error, false), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validar(String nombre, String correo, String clave) {
        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilClave.setError(null);
        if (nombre.length() < 2) {
            tilNombre.setError(getString(R.string.error_nombre_corto));
            etNombre.requestFocus();
            return false;
        }
        if (correo.isEmpty()) {
            tilCorreo.setError(getString(R.string.error_correo_vacio));
            etCorreo.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.error_correo_invalido));
            etCorreo.requestFocus();
            return false;
        }
        if (clave.length() < 6) {
            tilClave.setError(getString(R.string.error_clave_corta));
            etClave.requestFocus();
            return false;
        }
        return true;
    }

    private void enCarga(boolean cargando) {
        progreso.setVisibility(cargando ? View.VISIBLE : View.INVISIBLE);
        btnRegistrar.setEnabled(!cargando);
        btnVolver.setEnabled(!cargando);
    }
}
