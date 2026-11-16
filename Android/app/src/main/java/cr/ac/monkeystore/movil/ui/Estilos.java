package cr.ac.monkeystore.movil.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Locale;

import cr.ac.monkeystore.movil.R;

/** Colores e insignias consistentes en todas las pantallas. */
public final class Estilos {
    private Estilos() { }

    /** Color del estado del pedido (Pagado verde, Enviado azul...). */
    public static int colorEstado(Context c, String estado) {
        String e = estado == null ? "" : estado.toLowerCase(Locale.ROOT);
        int id;
        if (e.startsWith("pagado")) id = R.color.estado_verde;
        else if (e.startsWith("enviado")) id = R.color.estado_azul;
        else if (e.startsWith("entregado")) id = R.color.estado_gris;
        else if (e.startsWith("pendiente")) id = R.color.estado_ambar;
        else if (e.startsWith("cancelado")) id = R.color.estado_rojo;
        else id = R.color.texto_secundario;
        return ContextCompat.getColor(c, id);
    }

    /** Color estable para cada franquicia (mismo nombre, mismo color). */
    public static int colorFranquicia(Context c, String nombre) {
        TypedArray paleta = c.getResources().obtainTypedArray(R.array.paleta_franquicias);
        try {
            int i = Math.abs((nombre == null ? "" : nombre).hashCode()) % paleta.length();
            return paleta.getColor(i, ContextCompat.getColor(c, R.color.dorado));
        } finally {
            paleta.recycle();
        }
    }

    /** "Magic: The Gathering" → "MT"; "Pokémon TCG" → "PT". */
    public static String iniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String parte : nombre.trim().split("[\\s:!-]+")) {
            if (!parte.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }

    /** Pastilla: texto del color dado sobre el mismo color translúcido. */
    public static void pill(TextView tv, int color) {
        tv.setTextColor(color);
        tv.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 0x33)));
    }

    /** Insignia circular con iniciales y el color de la franquicia. */
    public static void insignia(TextView tv, String franquicia) {
        tv.setText(iniciales(franquicia));
        tv.setBackgroundTintList(ColorStateList.valueOf(colorFranquicia(tv.getContext(), franquicia)));
    }

    public static void mostrar(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
