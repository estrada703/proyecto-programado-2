package cr.ac.sabortico.movil.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Android 15+ dibuja la aplicación de borde a borde: sin este ajuste el contenido
 * queda debajo de la barra de estado. Aplica los márgenes del sistema como relleno.
 */
public final class Bordes {
    private Bordes() { }

    /** @param incluirInferior false cuando un hijo (BottomNavigationView) ya maneja la barra inferior */
    public static void aplicar(View raiz, final boolean incluirInferior) {
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            int inferior = incluirInferior ? Math.max(barras.bottom, teclado.bottom) : 0;
            v.setPadding(barras.left, barras.top, barras.right, inferior);
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);
    }
}
