package cr.ac.monkeystore.movil.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Formatos de presentación en español de Costa Rica. */
public final class Formato {
    private static final Locale CR = new Locale("es", "CR");

    private Formato() { }

    /** 48000 → "₡48 000". Los precios de la tienda son en colones enteros. */
    public static String dinero(double monto) {
        NumberFormat nf = NumberFormat.getNumberInstance(CR);
        nf.setMaximumFractionDigits(0);
        return "₡" + nf.format(Math.round(monto));
    }

    /** "2026-11-10T14:30:00" → "10 nov 2026 · 14:30". */
    public static String fecha(String iso) {
        Date d = leer(iso);
        if (d == null) {
            return iso == null ? "" : iso.replace('T', ' ');
        }
        return new SimpleDateFormat("d MMM yyyy · HH:mm", CR).format(d);
    }

    /** Fecha corta para las filas: "10 nov · 14:30". */
    public static String fechaCorta(String iso) {
        Date d = leer(iso);
        return d == null ? fecha(iso) : new SimpleDateFormat("d MMM · HH:mm", CR).format(d);
    }

    private static Date leer(String iso) {
        if (iso == null || iso.length() < 19) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).parse(iso.substring(0, 19));
        } catch (ParseException e) {
            return null;
        }
    }
}
