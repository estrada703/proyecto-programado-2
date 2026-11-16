package cr.ac.sabortico.movil.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import cr.ac.sabortico.movil.R;
import cr.ac.sabortico.movil.datos.local.CacheListado;
import cr.ac.sabortico.movil.datos.remoto.Callback;
import cr.ac.sabortico.movil.datos.remoto.ErrorApi;
import cr.ac.sabortico.movil.datos.remoto.ResultadoCompras;
import cr.ac.sabortico.movil.modelo.Compra;
import cr.ac.sabortico.movil.modelo.CompraParser;
import cr.ac.sabortico.movil.modelo.FiltroCompras;
import cr.ac.sabortico.movil.modelo.Sesion;
import cr.ac.sabortico.movil.util.Bitacora;
import cr.ac.sabortico.movil.util.Fabrica;
import cr.ac.sabortico.movil.util.Mensajes;

/** Listado de compras y reservas con filtro por sede y búsqueda por producto (F3, F5, E1). */
public class ListadoFragment extends Fragment {
    private CompraAdapter adaptador;
    private EditText etBuscar;
    private Spinner spCatalogo;
    private TextView tvVacio;
    private TextView tvBanner;
    private ProgressBar progreso;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_listado, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        etBuscar = vista.findViewById(R.id.etBuscar);
        spCatalogo = vista.findViewById(R.id.spCatalogo);
        tvVacio = vista.findViewById(R.id.tvVacio);
        tvBanner = vista.findViewById(R.id.tvBanner);
        progreso = vista.findViewById(R.id.progreso);
        Button btnActualizar = vista.findViewById(R.id.btnActualizar);
        RecyclerView rv = vista.findViewById(R.id.rvCompras);

        adaptador = new CompraAdapter(c -> ((MainActivity) requireActivity()).abrirDetalle(c));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adaptador);

        etBuscar.setText(Sesion.obtener().getFiltroTexto());
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                Sesion.obtener().setFiltroTexto(s.toString());
                aplicarFiltro();
            }
        });
        btnActualizar.setOnClickListener(v -> cargar());

        if (Sesion.obtener().getCompras() != null) {
            // ya están en memoria: el filtro no vuelve a descargar nada (F5)
            prepararSpinner();
            aplicarFiltro();
        } else {
            cargar();
        }
    }

    private void cargar() {
        progreso.setVisibility(View.VISIBLE);
        tvBanner.setVisibility(View.GONE);
        Bitacora.i(Bitacora.TAG_LISTADO, "Cargando listado de compras");
        Fabrica.compras(requireContext()).obtenerCompras(Sesion.obtener().getToken(),
                new Callback<ResultadoCompras>() {
                    @Override
                    public void alExito(ResultadoCompras r) {
                        if (!isAdded()) return;
                        progreso.setVisibility(View.INVISIBLE);
                        Sesion.obtener().setCompras(r.getCompras());
                        Fabrica.cache(requireContext()).guardar(r.getJson());
                        prepararSpinner();
                        aplicarFiltro();
                    }

                    @Override
                    public void alError(ErrorApi error) {
                        if (!isAdded()) return;
                        progreso.setVisibility(View.INVISIBLE);
                        manejarError(error);
                    }
                });
    }

    private void manejarError(ErrorApi error) {
        if (error.getTipo() == ErrorApi.Tipo.NO_AUTORIZADO) {
            Toast.makeText(requireContext(), R.string.api_401_sesion, Toast.LENGTH_LONG).show();
            ((MainActivity) requireActivity()).cerrarSesion();
            return;
        }
        boolean sinConexion = error.getTipo() == ErrorApi.Tipo.RED
                || error.getTipo() == ErrorApi.Tipo.TIEMPO_AGOTADO
                || error.getTipo() == ErrorApi.Tipo.SERVIDOR;
        if (sinConexion && mostrarDesdeCache()) {
            return;
        }
        Toast.makeText(requireContext(), Mensajes.paraError(requireContext(), error, true),
                Toast.LENGTH_LONG).show();
        if (Sesion.obtener().getCompras() == null) {
            tvVacio.setText(Mensajes.paraError(requireContext(), error, true));
            tvVacio.setVisibility(View.VISIBLE);
        }
    }

    /** E1: muestra el último listado guardado marcándolo como "sin conexión". */
    private boolean mostrarDesdeCache() {
        CacheListado cache = Fabrica.cache(requireContext());
        String json = cache.leerJson();
        if (json == null) {
            return false;
        }
        try {
            List<Compra> lista = CompraParser.parsear(json);
            Sesion.obtener().setCompras(lista);
            String hora = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date(cache.leerFecha()));
            tvBanner.setText(getString(R.string.banner_sin_conexion, hora));
            tvBanner.setVisibility(View.VISIBLE);
            Bitacora.w(Bitacora.TAG_LISTADO, "Sin conexión: se muestra la caché");
            prepararSpinner();
            aplicarFiltro();
            return true;
        } catch (JSONException e) {
            Bitacora.e(Bitacora.TAG_LISTADO, "La caché del listado está dañada", e);
            return false;
        }
    }

    private void prepararSpinner() {
        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.todas_las_sedes));
        TreeSet<String> sedes = new TreeSet<>();
        for (Compra c : Sesion.obtener().getCompras()) {
            if (c.getCatalogo() != null && !c.getCatalogo().isEmpty()) {
                sedes.add(c.getCatalogo());
            }
        }
        opciones.addAll(sedes);

        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, opciones);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCatalogo.setAdapter(a);
        int indice = Math.max(0, opciones.indexOf(Sesion.obtener().getFiltroCatalogo()));
        spCatalogo.setSelection(indice, false);
        spCatalogo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int posicion, long id) {
                Sesion.obtener().setFiltroCatalogo(posicion == 0 ? "" : (String) p.getItemAtPosition(posicion));
                aplicarFiltro();
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) { }
        });
    }

    private void aplicarFiltro() {
        List<Compra> todas = Sesion.obtener().getCompras();
        if (todas == null || adaptador == null) {
            return;
        }
        List<Compra> filtradas = FiltroCompras.filtrar(todas,
                Sesion.obtener().getFiltroCatalogo(), Sesion.obtener().getFiltroTexto());
        adaptador.establecer(filtradas);
        if (todas.isEmpty()) {
            tvVacio.setText(R.string.lista_vacia);
            tvVacio.setVisibility(View.VISIBLE);
        } else if (filtradas.isEmpty()) {
            tvVacio.setText(R.string.sin_resultados);
            tvVacio.setVisibility(View.VISIBLE);
        } else {
            tvVacio.setVisibility(View.GONE);
        }
    }
}
