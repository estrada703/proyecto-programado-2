package cr.ac.monkeystore.movil.datos.remoto;

import java.util.List;

import cr.ac.monkeystore.movil.modelo.Producto;

/** Operaciones de administración: estado de pedidos e inventario. */
public interface ServicioGestion {
    /** PUT /api/movil/pedidos/{id}/estado. Devuelve el nombre del nuevo estado. */
    void cambiarEstadoPedido(String token, int pedidoId, String estado, Callback<String> callback);

    /** GET /api/movil/productos. */
    void obtenerProductos(String token, Callback<List<Producto>> callback);

    /** PUT /api/movil/productos/{id}/stock. Devuelve el producto actualizado. */
    void actualizarStock(String token, int productoId, int stock, Callback<Producto> callback);
}
