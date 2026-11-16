package cr.ac.sabortico.movil.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cr.ac.sabortico.movil.R;
import cr.ac.sabortico.movil.modelo.Compra;
import cr.ac.sabortico.movil.util.Formato;

/** Adaptador del RecyclerView: cada fila muestra producto, fecha y monto. */
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

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View v = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_compra, padre, false);
        return new Fila(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        final Compra c = datos.get(posicion);
        fila.producto.setText(c.getProducto());
        fila.fecha.setText(Formato.fecha(c.getFecha()));
        fila.monto.setText(Formato.dinero(c.getMonto()));
        fila.itemView.setOnClickListener(v -> escucha.alTocar(c));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView producto;
        final TextView fecha;
        final TextView monto;

        Fila(View v) {
            super(v);
            producto = v.findViewById(R.id.tvProducto);
            fecha = v.findViewById(R.id.tvFecha);
            monto = v.findViewById(R.id.tvMonto);
        }
    }
}
