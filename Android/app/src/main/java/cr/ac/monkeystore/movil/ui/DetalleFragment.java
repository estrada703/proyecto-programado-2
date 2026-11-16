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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.datos.remoto.Callback;
import cr.ac.monkeystore.movil.datos.remoto.ErrorApi;
import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.modelo.Sesion;
import cr.ac.monkeystore.movil.modelo.TransicionesPedido;
import cr.ac.monkeystore.movil.util.Fabrica;
import cr.ac.monkeystore.movil.util.Formato;
import cr.ac.monkeystore.movil.util.Mensajes;

/**
 * Detalle del producto elegido: los datos llegan en los argumentos, no se vuelven a pedir (F4, T3).
 * Desde aquí el administrador también cambia el estado del pedido.
 */
public class DetalleFragment extends Fragment {
    private static final String ARG_COMPRA = "compra";
    private Compra compra;

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
        compra = c;
        Estilos.insignia(v.findViewById(R.id.tvDetInsignia), c.getCatalogo());
        texto(v, R.id.tvDetCatalogo, c.getCatalogo());
        texto(v, R.id.tvDetProducto, c.getProducto());

        mostrarEstado(v);

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

    /** Pinta el estado actual y los botones de los cambios permitidos. */
    private void mostrarEstado(View v) {
        TextView estadoPedido = v.findViewById(R.id.tvDetEstado);
        estadoPedido.setText(compra.getEstado());
        Estilos.pill(estadoPedido, Estilos.colorEstado(requireContext(), compra.getEstado()));

        LinearLayout acciones = v.findViewById(R.id.contAcciones);
        View sinAcciones = v.findViewById(R.id.tvSinAcciones);
        for (int i = acciones.getChildCount() - 1; i >= 0; i--) {
            if (acciones.getChildAt(i) != sinAcciones) acciones.removeViewAt(i);
        }
        LayoutInflater inf = LayoutInflater.from(requireContext());
        for (final String destino : TransicionesPedido.siguientes(compra.getEstado())) {
            boolean peligro = TransicionesPedido.CANCELADO.equals(destino);
            MaterialButton b = (MaterialButton) inf.inflate(
                    peligro ? R.layout.boton_peligro : R.layout.boton_accion, acciones, false);
            b.setText(textoAccion(destino));
            b.setOnClickListener(x -> confirmar(destino));
            acciones.addView(b);
        }
        Estilos.mostrar(sinAcciones, acciones.getChildCount() == 1);
    }

    private static int textoAccion(String destino) {
        switch (destino) {
            case TransicionesPedido.PAGADO: return R.string.accion_Pagado;
            case TransicionesPedido.ENVIADO: return R.string.accion_Enviado;
            case TransicionesPedido.ENTREGADO: return R.string.accion_Entregado;
            default: return R.string.accion_Cancelado;
        }
    }

    private void confirmar(final String destino) {
        boolean cancelar = TransicionesPedido.CANCELADO.equals(destino);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirmar_titulo, compra.getPedido()))
                .setMessage(cancelar ? getString(R.string.confirmar_cancelar)
                        : getString(R.string.confirmar_estado, destino))
                .setNegativeButton(R.string.boton_cancelar_dialogo, null)
                .setPositiveButton(R.string.boton_confirmar, (d, w) -> cambiarEstado(destino))
                .show();
    }

    private void cambiarEstado(String destino) {
        final View v = requireView();
        final View progreso = v.findViewById(R.id.progresoGestion);
        progreso.setVisibility(View.VISIBLE);
        Fabrica.gestion(requireContext()).cambiarEstadoPedido(Sesion.obtener().getToken(),
                compra.getPedidoId(), destino, new Callback<String>() {
                    @Override
                    public void alExito(String nuevo) {
                        if (!isAdded()) return;
                        progreso.setVisibility(View.INVISIBLE);
                        compra = compra.conEstado(nuevo);
                        requireArguments().putSerializable(ARG_COMPRA, compra); // sobrevive al girar
                        Sesion.obtener().reemplazarEstado(compra.getPedidoId(), nuevo);
                        mostrarEstado(v);
                        Toast.makeText(requireContext(),
                                getString(R.string.msg_estado_ok, compra.getPedido(), nuevo), Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).alCambiarPedido();
                    }

                    @Override
                    public void alError(ErrorApi error) {
                        if (!isAdded()) return;
                        progreso.setVisibility(View.INVISIBLE);
                        if (error.getTipo() == ErrorApi.Tipo.NO_AUTORIZADO) {
                            ((MainActivity) requireActivity()).cerrarSesion();
                            return;
                        }
                        Toast.makeText(requireContext(), Mensajes.paraError(requireContext(), error, true),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private static void texto(View raiz, int id, String valor) {
        ((TextView) raiz.findViewById(id)).setText(valor);
    }
}
