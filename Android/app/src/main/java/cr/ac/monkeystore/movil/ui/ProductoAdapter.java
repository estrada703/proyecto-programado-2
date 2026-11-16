package cr.ac.monkeystore.movil.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.modelo.Producto;
import cr.ac.monkeystore.movil.util.Formato;

/** Tarjetas del inventario con el stock en una pastilla de color (verde, ámbar o rojo). */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.Fila> {
    public interface AlTocar {
        void alTocar(Producto producto);
    }

    private final List<Producto> datos = new ArrayList<>();
    private final AlTocar escucha;

    public ProductoAdapter(AlTocar escucha) {
        this.escucha = escucha;
    }

    public void establecer(List<Producto> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    static int colorNivel(Context c, Producto p) {
        switch (p.nivel()) {
            case AGOTADO: return ContextCompat.getColor(c, R.color.estado_rojo);
            case BAJO: return ContextCompat.getColor(c, R.color.estado_ambar);
            default: return ContextCompat.getColor(c, R.color.estado_verde);
        }
    }

    static String textoStock(Context c, Producto p) {
        return p.getStock() <= 0 ? c.getString(R.string.stock_agotado)
                : c.getString(R.string.stock_unidades, p.getStock());
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Fila(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_producto, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila f, int posicion) {
        final Producto p = datos.get(posicion);
        Context c = f.itemView.getContext();
        String sep = c.getString(R.string.separador);
        StringBuilder sub = new StringBuilder(p.getCatalogo());
        if (!p.getTipo().isEmpty()) sub.append(sep).append(p.getTipo());
        if (!p.getCondicion().isEmpty()) sub.append(sep).append(p.getCondicion());
        if (p.esPreventa()) sub.append(sep).append(c.getString(R.string.preventa));

        Estilos.insignia(f.insignia, p.getCatalogo());
        f.nombre.setText(p.getNombre());
        f.subtitulo.setText(sub);
        f.precio.setText(Formato.dinero(p.getPrecio()));
        f.stock.setText(textoStock(c, p));
        Estilos.pill(f.stock, colorNivel(c, p));
        f.itemView.setContentDescription(p.getNombre() + sep + sub + sep + textoStock(c, p));
        f.itemView.setOnClickListener(v -> escucha.alTocar(p));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView insignia;
        final TextView nombre;
        final TextView subtitulo;
        final TextView precio;
        final TextView stock;

        Fila(View v) {
            super(v);
            insignia = v.findViewById(R.id.tvInsignia);
            nombre = v.findViewById(R.id.tvNombre);
            subtitulo = v.findViewById(R.id.tvSubtitulo);
            precio = v.findViewById(R.id.tvPrecio);
            stock = v.findViewById(R.id.tvStock);
        }
    }
}
