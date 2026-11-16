package cr.ac.monkeystore.movil.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.util.Formato;

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
        Estilos.insignia(v.findViewById(R.id.tvDetInsignia), c.getCatalogo());
        texto(v, R.id.tvDetCatalogo, c.getCatalogo());
        texto(v, R.id.tvDetProducto, c.getProducto());

        TextView estadoPedido = v.findViewById(R.id.tvDetEstado);
        estadoPedido.setText(c.getEstado());
        Estilos.pill(estadoPedido, Estilos.colorEstado(requireContext(), c.getEstado()));

        int gris = ContextCompat.getColor(requireContext(), R.color.texto_secundario);
        TextView tipo = v.findViewById(R.id.tvDetTipo);
        tipo.setText(c.getTipo());
        Estilos.pill(tipo, gris);
        Estilos.mostrar(tipo, !c.getTipo().isEmpty());

        TextView condicion = v.findViewById(R.id.tvDetCondicion);
        condicion.setText(c.getCondicion());
        Estilos.pill(condicion, gris);
        Estilos.mostrar(condicion, !c.getCondicion().isEmpty());

        TextView reserva = v.findViewById(R.id.tvDetReserva);
        Estilos.pill(reserva, ContextCompat.getColor(requireContext(), R.color.dorado));
        Estilos.mostrar(reserva, c.esReserva());

        texto(v, R.id.tvDetMonto, Formato.dinero(c.getMonto()));
        texto(v, R.id.tvDetCalculo, getString(R.string.calculo_monto, c.getCantidad(), Formato.dinero(c.getPrecio())));
        texto(v, R.id.tvDetPedido, c.getPedido());
        texto(v, R.id.tvDetCliente, c.getCliente());
        texto(v, R.id.tvDetFecha, Formato.fecha(c.getFecha()));
        texto(v, R.id.tvDetPrecio, Formato.dinero(c.getPrecio()));
        texto(v, R.id.tvDetCantidad, String.valueOf(c.getCantidad()));
        texto(v, R.id.tvDetDescripcion, c.getDescripcion());
    }

    private static void texto(View raiz, int id, String valor) {
        ((TextView) raiz.findViewById(id)).setText(valor);
    }
}
