package cr.ac.monkeystore.movil.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;
import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.modelo.Resumen;
import cr.ac.monkeystore.movil.modelo.Sesion;
import cr.ac.monkeystore.movil.util.Formato;
import cr.ac.monkeystore.movil.util.Mensajes;

/** Panel de resumen: indicadores de la tienda calculados del listado ya descargado. */
public class ResumenFragment extends Fragment {
    private static final int TOP = 5;

    private SwipeRefreshLayout swipe;
    private View progreso;
    private TextView tvBanner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_resumen, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);
        swipe = v.findViewById(R.id.swipe);
        progreso = v.findViewById(R.id.progreso);
        tvBanner = v.findViewById(R.id.tvBanner);
        ((TextView) v.findViewById(R.id.tvSaludo)).setText(
                getString(R.string.saludo, primerNombre(Sesion.obtener().getNombre())));
        etiqueta(v, R.id.kpiIngresos, R.string.kpi_ingresos);
        etiqueta(v, R.id.kpiPedidos, R.string.kpi_pedidos);
        etiqueta(v, R.id.kpiTicket, R.string.kpi_ticket);
        etiqueta(v, R.id.kpiArticulos, R.string.kpi_articulos);

        swipe.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.sobre_dorado));
        swipe.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.dorado));
        swipe.setOnRefreshListener(() -> cargar(true));
        cargar(false);
    }

    private static String primerNombre(String nombre) {
        if (nombre == null) return "";
        String n = nombre.trim();
        int espacio = n.indexOf(' ');
        return espacio > 0 ? n.substring(0, espacio) : n;
    }

    private void etiqueta(View raiz, int idKpi, int texto) {
        ((TextView) raiz.findViewById(idKpi).findViewById(R.id.tvKpiEtiqueta)).setText(texto);
    }

    private void valor(int idKpi, String texto, @Nullable String nota) {
        View kpi = requireView().findViewById(idKpi);
        ((TextView) kpi.findViewById(R.id.tvKpiValor)).setText(texto);
        TextView tvNota = kpi.findViewById(R.id.tvKpiNota);
        tvNota.setText(nota);
        Estilos.mostrar(tvNota, nota != null);
    }

    private void cargar(boolean forzar) {
        if (!forzar) progreso.setVisibility(View.VISIBLE);
        ((MainActivity) requireActivity()).cargarCompras(forzar, new MainActivity.EscuchaCompras() {
            @Override
            public void alCargar(List<Compra> compras, long fechaCache) {
                if (!isAdded()) return;
                progreso.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                if (fechaCache > 0) {
                    String hora = new SimpleDateFormat("d MMM · HH:mm", Locale.getDefault()).format(new Date(fechaCache));
                    tvBanner.setText(getString(R.string.banner_sin_conexion, hora));
                }
                Estilos.mostrar(tvBanner, fechaCache > 0);
                mostrar(Resumen.calcular(compras, TOP));
            }

            @Override
            public void alFallar(ErrorApi error) {
                if (!isAdded()) return;
                progreso.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                Toast.makeText(requireContext(), Mensajes.paraError(requireContext(), error, true),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrar(Resumen r) {
        valor(R.id.kpiIngresos, Formato.dinero(r.getIngresos()), null);
        valor(R.id.kpiPedidos, String.valueOf(r.getPedidos()), null);
        valor(R.id.kpiTicket, Formato.dinero(r.getTicketPromedio()), null);
        valor(R.id.kpiArticulos, String.valueOf(r.getArticulos()),
                r.getReservas() > 0 ? getString(R.string.reservas_nota, r.getReservas()) : null);

        ((BarrasView) requireView().findViewById(R.id.barras)).establecer(r.getPorFranquicia());

        LayoutInflater inf = LayoutInflater.from(requireContext());
        LinearLayout top = requireView().findViewById(R.id.contMasVendidos);
        top.removeAllViews();
        if (r.getMasVendidos().isEmpty()) {
            TextView vacio = new TextView(requireContext());
            vacio.setText(R.string.sin_ventas);
            vacio.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
            top.addView(vacio);
        }
        int posicion = 1;
        for (Resumen.Item item : r.getMasVendidos()) {
            View fila = inf.inflate(R.layout.item_ranking, top, false);
            ((TextView) fila.findViewById(R.id.tvPosicion)).setText(String.valueOf(posicion++));
            ((TextView) fila.findViewById(R.id.tvNombre)).setText(item.getNombre());
            ((TextView) fila.findViewById(R.id.tvValor)).setText(getString(R.string.unidades, (int) item.getValor()));
            top.addView(fila);
        }

        LinearLayout estados = requireView().findViewById(R.id.contEstados);
        estados.removeAllViews();
        for (Map.Entry<String, Integer> e : r.getPorEstado().entrySet()) {
            View fila = inf.inflate(R.layout.item_ranking, estados, false);
            TextView punto = fila.findViewById(R.id.tvPosicion);
            punto.setText("●");
            punto.setTextColor(Estilos.colorEstado(requireContext(), e.getKey()));
            ((TextView) fila.findViewById(R.id.tvNombre)).setText(e.getKey());
            TextView cantidad = fila.findViewById(R.id.tvValor);
            cantidad.setText(getString(R.string.pedidos_cantidad, e.getValue()));
            Estilos.pill(cantidad, Estilos.colorEstado(requireContext(), e.getKey()));
            estados.addView(fila);
        }
    }
}
