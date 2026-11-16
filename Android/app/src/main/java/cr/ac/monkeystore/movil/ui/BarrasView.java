package cr.ac.monkeystore.movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import cr.ac.monkeystore.movil.R;
import cr.ac.monkeystore.movil.modelo.Resumen;
import cr.ac.monkeystore.movil.util.Formato;

/** Gráfico de barras horizontales dibujado a mano (sin bibliotecas externas). */
public class BarrasView extends View {
    private final Paint pEtiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPista = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float altoFila;
    private final float grosor;
    private List<Resumen.Item> datos = new ArrayList<>();

    public BarrasView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        float texto = getResources().getDimension(R.dimen.texto_pequeno);
        pEtiqueta.setTextSize(texto);
        pEtiqueta.setColor(ContextCompat.getColor(c, R.color.texto));
        pValor.setTextSize(texto);
        pValor.setColor(ContextCompat.getColor(c, R.color.texto_secundario));
        pValor.setTextAlign(Paint.Align.RIGHT);
        pPista.setColor(ContextCompat.getColor(c, R.color.tarjeta_alta));
        altoFila = getResources().getDimension(R.dimen.barra_alto_fila);
        grosor = getResources().getDimension(R.dimen.barra_grosor);
    }

    public void establecer(List<Resumen.Item> nuevos) {
        datos = nuevos == null ? new ArrayList<>() : nuevos;
        StringBuilder desc = new StringBuilder(getContext().getString(R.string.descripcion_grafico));
        for (Resumen.Item i : datos) {
            desc.append(". ").append(i.getNombre()).append(": ").append(Formato.dinero(i.getValor()));
        }
        setContentDescription(desc);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int anchoSpec, int altoSpec) {
        int alto = (int) (getPaddingTop() + getPaddingBottom() + Math.max(1, datos.size()) * altoFila * 1.5f);
        setMeasuredDimension(MeasureSpec.getSize(anchoSpec), resolveSize(alto, altoSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datos.isEmpty()) {
            return;
        }
        double max = datos.get(0).getValor();
        for (Resumen.Item i : datos) max = Math.max(max, i.getValor());
        float izq = getPaddingLeft();
        float der = getWidth() - getPaddingRight();
        float y = getPaddingTop();
        float radio = grosor / 2f;
        for (Resumen.Item item : datos) {
            String valor = Formato.dinero(item.getValor());
            float anchoValor = pValor.measureText(valor);
            CharSequence nombre = TextUtils.ellipsize(item.getNombre(),
                    new android.text.TextPaint(pEtiqueta), der - izq - anchoValor - grosor, TextUtils.TruncateAt.END);
            canvas.drawText(nombre, 0, nombre.length(), izq, y + altoFila * 0.55f, pEtiqueta);
            canvas.drawText(valor, der, y + altoFila * 0.55f, pValor);

            float yb = y + altoFila * 0.8f;
            rect.set(izq, yb, der, yb + grosor);
            canvas.drawRoundRect(rect, radio, radio, pPista);
            float fraccion = max <= 0 ? 0 : (float) (item.getValor() / max);
            rect.set(izq, yb, izq + Math.max(grosor, (der - izq) * fraccion), yb + grosor);
            pBarra.setColor(Estilos.colorFranquicia(getContext(), item.getNombre()));
            canvas.drawRoundRect(rect, radio, radio, pBarra);
            y += altoFila * 1.5f;
        }
    }
}
