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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.remoto.Callback;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;
import cr.ac.monkeystore.movil.modelo.FiltroInventario;
import cr.ac.monkeystore.movil.modelo.Producto;
import cr.ac.monkeystore.movil.modelo.Sesion;
import cr.ac.monkeystore.movil.util.Fabrica;
import cr.ac.monkeystore.movil.util.Mensajes;

/** Inventario: stock de cada producto, alertas de stock bajo y edición de la cantidad. */
public class InventarioFragment extends Fragment {
    private static final String ESTADO_VISTA = "vista";

    private final List<Producto> productos = new ArrayList<>();
    private ProductoAdapter adaptador;
    private EditText etBuscar;
    private ChipGroup chips;
    private TextView tvConteo;
    private TextView tvVacio;
    private View progreso;
    private SwipeRefreshLayout swipe;
    private FiltroInventario.Vista vista = FiltroInventario.Vista.TODOS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_inventario, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);
        if (estado != null) {
            vista = FiltroInventario.Vista.valueOf(estado.getString(ESTADO_VISTA, vista.name()));
        }
        etBuscar = v.findViewById(R.id.etBuscar);
        chips = v.findViewById(R.id.chipsVista);
        tvConteo = v.findViewById(R.id.tvConteo);
        tvVacio = v.findViewById(R.id.tvVacio);
        progreso = v.findViewById(R.id.progreso);
        swipe = v.findViewById(R.id.swipe);
        RecyclerView rv = v.findViewById(R.id.rvProductos);

        adaptador = new ProductoAdapter(this::editarStock);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adaptador);

        swipe.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.sobre_dorado));
        swipe.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.dorado));
        swipe.setOnRefreshListener(this::cargar);

        agregarChip(R.string.vista_todos, FiltroInventario.Vista.TODOS);
        agregarChip(R.string.vista_bajo, FiltroInventario.Vista.STOCK_BAJO);
        agregarChip(R.string.vista_agotados, FiltroInventario.Vista.AGOTADOS);
        chips.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) return;
            vista = (FiltroInventario.Vista) g.findViewById(ids.get(0)).getTag();
            aplicarFiltro();
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { aplicarFiltro(); }
        });
        progreso.setVisibility(View.VISIBLE);
        cargar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle salida) {
        super.onSaveInstanceState(salida);
        salida.putString(ESTADO_VISTA, vista.name());
    }

    private void agregarChip(int texto, FiltroInventario.Vista valor) {
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
        chips.addView(chip);
        chip.setChecked(valor == vista);
    }

    private void cargar() {
        Fabrica.gestion(requireContext()).obtenerProductos(Sesion.obtener().getToken(),
                new Callback<List<Producto>>() {
                    @Override
                    public void alExito(List<Producto> lista) {
                        if (!isAdded()) return;
                        terminar();
                        productos.clear();
                        productos.addAll(lista);
                        aplicarFiltro();
                    }

                    @Override
                    public void alError(ErrorApi error) {
                        if (!isAdded()) return;
                        terminar();
                        manejarError(error);
                    }
                });
    }

    private void terminar() {
        progreso.setVisibility(View.INVISIBLE);
        swipe.setRefreshing(false);
    }

    private void manejarError(ErrorApi error) {
        if (error.getTipo() == ErrorApi.Tipo.NO_AUTORIZADO) {
            ((MainActivity) requireActivity()).cerrarSesion();
            return;
        }
        Toast.makeText(requireContext(), Mensajes.paraError(requireContext(), error, true),
                Toast.LENGTH_LONG).show();
    }

    private void aplicarFiltro() {
        List<Producto> filtrados = FiltroInventario.filtrar(productos, etBuscar.getText().toString(), vista);
        adaptador.establecer(filtrados);
        tvConteo.setText(getString(R.string.conteo_inventario, productos.size(), FiltroInventario.alertas(productos)));
        Estilos.mostrar(tvVacio, filtrados.isEmpty() && progreso.getVisibility() != View.VISIBLE);
    }

    /** Diálogo con − / cantidad / + y validación antes de enviar. */
    private void editarStock(final Producto p) {
        View contenido = getLayoutInflater().inflate(R.layout.dialogo_stock, null, false);
        final TextInputLayout til = contenido.findViewById(R.id.tilStock);
        final EditText et = contenido.findViewById(R.id.etStock);
        et.setText(String.valueOf(p.getStock()));
        contenido.findViewById(R.id.btnMenos).setOnClickListener(b -> ajustar(et, -1));
        contenido.findViewById(R.id.btnMas).setOnClickListener(b -> ajustar(et, 1));

        final AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(p.getNombre())
                .setMessage(R.string.titulo_stock)
                .setView(contenido)
                .setNegativeButton(R.string.boton_cancelar_dialogo, null)
                .setPositiveButton(R.string.boton_guardar, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            Integer valor = leer(et);
            if (valor == null || !Producto.stockValido(valor)) {
                til.setError(getString(R.string.error_stock));
                return;
            }
            dialogo.dismiss();
            guardar(p, valor);
        }));
        dialogo.show();
    }

    private static Integer leer(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void ajustar(EditText et, int delta) {
        Integer actual = leer(et);
        int nuevo = Math.max(0, Math.min(Producto.STOCK_MAXIMO, (actual == null ? 0 : actual) + delta));
        et.setText(String.valueOf(nuevo));
        et.setSelection(et.getText().length());
    }

    private void guardar(final Producto original, int valor) {
        progreso.setVisibility(View.VISIBLE);
        Fabrica.gestion(requireContext()).actualizarStock(Sesion.obtener().getToken(), original.getId(), valor,
                new Callback<Producto>() {
                    @Override
                    public void alExito(Producto actualizado) {
                        if (!isAdded()) return;
                        progreso.setVisibility(View.INVISIBLE);
                        for (int i = 0; i < productos.size(); i++) {
                            if (productos.get(i).getId() == actualizado.getId()) productos.set(i, actualizado);
                        }
                        aplicarFiltro();
                        Toast.makeText(requireContext(), getString(R.string.msg_stock_ok,
                                actualizado.getNombre(), actualizado.getStock()), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void alError(ErrorApi error) {
                        if (!isAdded()) return;
                        progreso.setVisibility(View.INVISIBLE);
                        manejarError(error);
                    }
                });
    }
}
