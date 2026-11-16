package cr.ac.monkeystore.movil.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;
import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.modelo.FiltroCompras;
import cr.ac.monkeystore.movil.modelo.Sesion;
import cr.ac.monkeystore.movil.util.Formato;
import cr.ac.monkeystore.movil.util.Mensajes;

/** Listado de productos comprados o reservados, con filtro por franquicia y búsqueda (F3, F5, E1). */
public class ListadoFragment extends Fragment {
    private CompraAdapter adaptador;
    private ChipGroup chips;
    private TextView tvVacio;
    private TextView tvBanner;
    private TextView tvConteo;
    private LinearProgressIndicator progreso;
    private SwipeRefreshLayout swipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_listado, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View vista, @Nullable Bundle estado) {
        super.onViewCreated(vista, estado);
        EditText etBuscar = vista.findViewById(R.id.etBuscar);
        chips = vista.findViewById(R.id.chipsFranquicia);
        tvVacio = vista.findViewById(R.id.tvVacio);
        tvBanner = vista.findViewById(R.id.tvBanner);
        tvConteo = vista.findViewById(R.id.tvConteo);
        progreso = vista.findViewById(R.id.progreso);
        swipe = vista.findViewById(R.id.swipe);
        RecyclerView rv = vista.findViewById(R.id.rvCompras);

        adaptador = new CompraAdapter(c -> ((MainActivity) requireActivity()).abrirDetalle(c));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adaptador);

        swipe.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.sobre_dorado));
        swipe.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.dorado));
        swipe.setOnRefreshListener(() -> cargar(true));

        etBuscar.setText(Sesion.obtener().getFiltroTexto());
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                Sesion.obtener().setFiltroTexto(s.toString());
                aplicarFiltro(); // en memoria: no vuelve a descargar (F5)
            }
        });
        cargar(false);
    }

    private void cargar(boolean forzar) {
        if (!forzar) progreso.setVisibility(View.VISIBLE);
        ((MainActivity) requireActivity()).cargarCompras(forzar, new MainActivity.EscuchaCompras() {
            @Override
            public void alCargar(List<Compra> compras, long fechaCache) {
                if (!isAdded()) return;
                terminarCarga();
                if (fechaCache > 0) {
                    String hora = new SimpleDateFormat("d MMM · HH:mm", Locale.getDefault()).format(new Date(fechaCache));
                    tvBanner.setText(getString(R.string.banner_sin_conexion, hora));
                }
                Estilos.mostrar(tvBanner, fechaCache > 0);
                prepararChips(compras);
                aplicarFiltro();
            }

            @Override
            public void alFallar(ErrorApi error) {
                if (!isAdded()) return;
                terminarCarga();
                String msg = Mensajes.paraError(requireContext(), error, true);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                if (Sesion.obtener().getCompras() == null) {
                    tvVacio.setText(msg);
                    tvVacio.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void terminarCarga() {
        progreso.setVisibility(View.INVISIBLE);
        swipe.setRefreshing(false);
    }

    /** Un chip por franquicia (catálogo 1) más "Todas". */
    private void prepararChips(List<Compra> compras) {
        TreeSet<String> franquicias = new TreeSet<>();
        for (Compra c : compras) {
            if (!c.getCatalogo().isEmpty()) franquicias.add(c.getCatalogo());
        }
        chips.setOnCheckedStateChangeListener(null);
        chips.removeAllViews();
        String elegida = Sesion.obtener().getFiltroCatalogo();
        agregarChip(getString(R.string.todas_las_franquicias), "", elegida.isEmpty() || !franquicias.contains(elegida));
        for (String f : franquicias) {
            agregarChip(f, f, f.equals(elegida));
        }
        chips.setOnCheckedStateChangeListener((grupo, ids) -> {
            if (ids.isEmpty()) return;
            Chip chip = grupo.findViewById(ids.get(0));
            Sesion.obtener().setFiltroCatalogo((String) chip.getTag());
            aplicarFiltro();
        });
    }

    private void agregarChip(String texto, String valor, boolean marcado) {
        Chip chip = new Chip(requireContext());
        chip.setId(View.generateViewId());
        chip.setText(texto);
        chip.setTag(valor);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_fondo));
        chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_texto));
        chip.setChipStrokeColorResource(R.color.borde);
        chip.setChipStrokeWidth(getResources().getDimension(R.dimen.borde_tarjeta));
        chip.setEnsureMinTouchTargetSize(true);
        chips.addView(chip);
        chip.setChecked(marcado);
    }

    private void aplicarFiltro() {
        List<Compra> todas = Sesion.obtener().getCompras();
        if (todas == null || adaptador == null) {
            return;
        }
        List<Compra> filtradas = FiltroCompras.filtrar(todas,
                Sesion.obtener().getFiltroCatalogo(), Sesion.obtener().getFiltroTexto());
        adaptador.establecer(filtradas);

        int unidades = 0;
        double total = 0;
        for (Compra c : filtradas) {
            unidades += c.getCantidad();
            total += c.getMonto();
        }
        tvConteo.setText(getString(R.string.conteo_listado, unidades, Formato.dinero(total)));

        if (todas.isEmpty()) {
            tvVacio.setText(R.string.lista_vacia);
        } else if (filtradas.isEmpty()) {
            tvVacio.setText(R.string.sin_resultados);
        }
        Estilos.mostrar(tvVacio, filtradas.isEmpty());
    }
}
