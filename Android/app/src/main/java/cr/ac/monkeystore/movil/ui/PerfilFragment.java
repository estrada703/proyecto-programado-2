package cr.ac.monkeystore.movil.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cr.ac.monkeystore.movil.BuildConfig;
import cr.ac.monkeystore.movil.R;

/** Perfil del administrador, datos de seguridad y cierre de sesión (F6). */
public class PerfilFragment extends Fragment {
    private static final String ARG_NOMBRE = "nombre";
    private static final String ARG_CORREO = "correo";

    public static PerfilFragment nuevo(String nombre, String correo) {
        PerfilFragment f = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOMBRE, nombre);
        args.putString(ARG_CORREO, correo);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup contenedor,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_perfil, contenedor, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle estado) {
        super.onViewCreated(v, estado);
        String nombre = requireArguments().getString(ARG_NOMBRE);
        ((TextView) v.findViewById(R.id.tvAvatar)).setText(Estilos.iniciales(nombre));
        ((TextView) v.findViewById(R.id.tvPerfilNombre)).setText(nombre);
        ((TextView) v.findViewById(R.id.tvPerfilCorreo)).setText(requireArguments().getString(ARG_CORREO));
        ((TextView) v.findViewById(R.id.tvPerfilServidor)).setText(BuildConfig.API_BASE_URL);
        ((TextView) v.findViewById(R.id.tvPerfilCifrado)).setText(R.string.valor_cifrado);
        ((TextView) v.findViewById(R.id.tvPerfilAlmacen)).setText(R.string.valor_almacen);
        ((TextView) v.findViewById(R.id.tvPerfilVersion)).setText(BuildConfig.VERSION_NAME);
        v.findViewById(R.id.btnCerrarSesion).setOnClickListener(
                b -> ((MainActivity) requireActivity()).cerrarSesion());
    }
}
