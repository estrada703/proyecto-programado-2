package cr.ac.sabortico.movil.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cr.ac.sabortico.movil.R;
import cr.ac.sabortico.movil.datos.local.RepositorioCredenciales;
import cr.ac.sabortico.movil.datos.remoto.Callback;
import cr.ac.sabortico.movil.datos.remoto.ErrorApi;
import cr.ac.sabortico.movil.datos.remoto.ResultadoAuth;
import cr.ac.sabortico.movil.datos.remoto.ServicioAutenticacion;
import cr.ac.sabortico.movil.modelo.Sesion;
import cr.ac.sabortico.movil.util.Bitacora;
import cr.ac.sabortico.movil.util.Fabrica;
import cr.ac.sabortico.movil.util.Mensajes;

/** Pantalla de ingreso con correo y contraseña (F1). Siempre arranca sin sesión (F6). */
public class LoginActivity extends AppCompatActivity {
    private EditText etCorreo;
    private EditText etClave;
    private Button btnIngresar;
    private Button btnIrRegistro;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Sesion.obtener().cerrar(); // al abrir la app siempre se pide ingresar

        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        progreso = findViewById(R.id.progreso);

        btnIngresar.setOnClickListener(v -> intentarIngreso());
        btnIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarIngreso() {
        final String correo = etCorreo.getText().toString().trim();
        String clave = etClave.getText().toString();
        if (!validar(correo, clave)) {
            return;
        }

        RepositorioCredenciales repo = Fabrica.credenciales(this);
        if (!repo.existe(correo)) {
            Bitacora.w(Bitacora.TAG_INGRESO, "Ingreso rechazado: administrador no registrado en el dispositivo");
            Toast.makeText(this, R.string.msg_no_registrado, Toast.LENGTH_LONG).show();
            return;
        }
        if (!repo.verificar(correo, clave)) {
            Bitacora.w(Bitacora.TAG_INGRESO, "Ingreso rechazado: contraseña local incorrecta");
            Toast.makeText(this, R.string.msg_clave_local_incorrecta, Toast.LENGTH_LONG).show();
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
        servicio.ingresar(correo, clave, new Callback<ResultadoAuth>() {
            @Override
            public void alExito(ResultadoAuth r) {
                enCarga(false);
                Bitacora.i(Bitacora.TAG_INGRESO, "Ingreso aceptado");
                Sesion.obtener().iniciar(r.getToken(), r.getNombre(), correo);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_NOMBRE, r.getNombre());
                intent.putExtra(MainActivity.EXTRA_CORREO, correo);
                startActivity(intent);
                finish();
            }

            @Override
            public void alError(ErrorApi error) {
                enCarga(false);
                Bitacora.w(Bitacora.TAG_INGRESO, "Ingreso rechazado: " + error.getTipo());
                Toast.makeText(LoginActivity.this,
                        Mensajes.paraError(LoginActivity.this, error, false), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validar(String correo, String clave) {
        if (correo.isEmpty()) {
            etCorreo.setError(getString(R.string.error_correo_vacio));
            etCorreo.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError(getString(R.string.error_correo_invalido));
            etCorreo.requestFocus();
            return false;
        }
        if (clave.isEmpty()) {
            etClave.setError(getString(R.string.error_clave_vacia));
            etClave.requestFocus();
            return false;
        }
        return true;
    }

    private void enCarga(boolean cargando) {
        progreso.setVisibility(cargando ? View.VISIBLE : View.INVISIBLE);
        btnIngresar.setEnabled(!cargando);
        btnIrRegistro.setEnabled(!cargando);
    }
}
