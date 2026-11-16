package cr.ac.sabortico.movil.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cr.ac.sabortico.movil.R;
import cr.ac.sabortico.movil.modelo.Compra;
import cr.ac.sabortico.movil.util.Formato;

/** Detalle del producto elegido: los datos llegan en los argumentos, no se vuelven a pedir (F4, T3). */
public class DetalleFragment extends Fragment {
    private static final String ARG_COMPRA = "compra";

    public static DetalleFragment nuevo(Compra compra) {
        DetalleFragment f = new DetalleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COMPRA, compra);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_detalle, contenedor, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);
        Compra c = (Compra) requireArguments().getSerializable(ARG_COMPRA);
        if (c == null) {
            return;
        }
        ((TextView) v.findViewById(R.id.tvDetProducto)).setText(c.getProducto());
        ((TextView) v.findViewById(R.id.tvDetTipo)).setText(c.getTipo());
        ((TextView) v.findViewById(R.id.tvDetDescripcion)).setText(c.getDescripcion());
        ((TextView) v.findViewById(R.id.tvDetPrecio)).setText(Formato.dinero(c.getPrecio()));
        ((TextView) v.findViewById(R.id.tvDetCantidad)).setText(String.valueOf(c.getCantidad()));
        ((TextView) v.findViewById(R.id.tvDetFecha)).setText(Formato.fecha(c.getFecha()));
        ((TextView) v.findViewById(R.id.tvDetMonto)).setText(Formato.dinero(c.getMonto()));
        ((TextView) v.findViewById(R.id.tvDetCatalogo)).setText(c.getCatalogo());
    }
}
