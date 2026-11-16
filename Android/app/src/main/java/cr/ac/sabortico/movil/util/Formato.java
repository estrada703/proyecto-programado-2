package cr.ac.sabortico.movil.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formatos de presentación. */
public final class Formato {
    private Formato() { }

    public static String dinero(double monto) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("es", "CR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "₡" + nf.format(monto);
    }

    /** Recorta "2026-11-10T14:30:00" a "2026-11-10 14:30". */
    public static String fecha(String iso) {
        if (iso == null) return "";
        String s = iso.replace('T', ' ');
        return s.length() > 16 ? s.substring(0, 16) : s;
    }
}
