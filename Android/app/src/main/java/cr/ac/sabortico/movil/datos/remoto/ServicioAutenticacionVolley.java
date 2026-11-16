package cr.ac.sabortico.movil.datos.remoto;

/** Implementación con Volley. La actividad solo conoce {@link ServicioAutenticacion}. */
public class ServicioAutenticacionVolley implements ServicioAutenticacion {
    private final PeticionCifrada peticionValidar;
    private final PeticionCifrada peticionIngresar;

    public ServicioAutenticacionVolley(PeticionCifrada peticionValidar, PeticionCifrada peticionIngresar) {
        this.peticionValidar = peticionValidar;
        this.peticionIngresar = peticionIngresar;
    }

    @Override
    public void validar(String correo, String clave, Callback<ResultadoAuth> callback) {
        peticionValidar.enviar(correo, clave, callback);
    }

    @Override
    public void ingresar(String correo, String clave, Callback<ResultadoAuth> callback) {
        peticionIngresar.enviar(correo, clave, callback);
    }
}
