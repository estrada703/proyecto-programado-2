package cr.ac.monkeystore.movil.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.modelo.Compra;
import cr.ac.monkeystore.movil.util.Formato;

/** Adaptador del RecyclerView: cada tarjeta muestra producto, fecha, monto y estado. */
public class CompraAdapter extends RecyclerView.Adapter<CompraAdapter.Fila> {
    public interface AlTocar {
        void alTocar(Compra compra);
    }

    private final List<Compra> datos = new ArrayList<>();
    private final AlTocar escucha;

    public CompraAdapter(AlTocar escucha) {
        this.escucha = escucha;
    }

    public void establecer(List<Compra> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /** "Pokémon TCG · Carta suelta · Nueva" sin partes vacías. */
    static String subtitulo(Context c, Compra compra) {
        String sep = c.getString(R.string.separador);
        StringBuilder sb = new StringBuilder(compra.getCatalogo());
        if (!compra.getTipo().isEmpty()) sb.append(sep).append(compra.getTipo());
        if (!compra.getCondicion().isEmpty()) sb.append(sep).append(compra.getCondicion());
        return sb.toString();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View v = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_compra, padre, false);
        return new Fila(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        final Compra c = datos.get(posicion);
        Context ctx = fila.itemView.getContext();
        String sep = ctx.getString(R.string.separador);
        Estilos.insignia(fila.insignia, c.getCatalogo());
        fila.producto.setText(c.getProducto());
        fila.subtitulo.setText(subtitulo(ctx, c));
        fila.fecha.setText(Formato.fechaCorta(c.getFecha()) + sep + c.getPedido());
        fila.monto.setText(Formato.dinero(c.getMonto()));
        fila.estado.setText(c.getEstado());
        Estilos.pill(fila.estado, Estilos.colorEstado(ctx, c.getEstado()));
        fila.itemView.setContentDescription(c.getProducto() + sep + subtitulo(ctx, c) + sep
                + Formato.dinero(c.getMonto()) + sep + c.getEstado());
        fila.itemView.setOnClickListener(v -> escucha.alTocar(c));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView insignia;
        final TextView producto;
        final TextView subtitulo;
        final TextView fecha;
        final TextView monto;
        final TextView estado;

        Fila(View v) {
            super(v);
            insignia = v.findViewById(R.id.tvInsignia);
            producto = v.findViewById(R.id.tvProducto);
            subtitulo = v.findViewById(R.id.tvSubtitulo);
            fecha = v.findViewById(R.id.tvFecha);
            monto = v.findViewById(R.id.tvMonto);
            estado = v.findViewById(R.id.tvEstado);
        }
    }
}
