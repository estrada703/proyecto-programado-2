using System.ComponentModel.DataAnnotations;

namespace TheMonkeyStore.Movil.Api.Dtos;

/// <summary>Cuerpo de los endpoints POST: { "datos": "Base64(IV || cifrado || etiqueta)" }.</summary>
public class DatosCifradosDto
{
    [Required]
    public string Datos { get; set; } = string.Empty;
}

/// <summary>Credenciales ya descifradas.</summary>
public class CredencialesDto
{
    public string Correo { get; set; } = string.Empty;
    public string Clave { get; set; } = string.Empty;
}

public record ValidarRespuestaDto(bool Valido, string Nombre);

public record IngresoRespuestaDto(string Nombre, string Token);

/// <summary>Fila del listado móvil de compras y reservas.</summary>
public class CompraMovilDto
{
    public int Id { get; set; }
    public string Producto { get; set; } = string.Empty;
    public string Descripcion { get; set; } = string.Empty;
    public decimal Precio { get; set; }
    public int Cantidad { get; set; }
    public DateTime Fecha { get; set; }
    public decimal Monto { get; set; }
    /// <summary>Catálogo 1: nombre de la franquicia.</summary>
    public string Catalogo { get; set; } = string.Empty;
    /// <summary>Tipo de producto: Sobre, Caja, Carta suelta...</summary>
    public string Tipo { get; set; } = string.Empty;
    /// <summary>Identificador del pedido (para cambiar su estado).</summary>
    public int PedidoId { get; set; }
    /// <summary>Número de pedido, por ejemplo TMS-000012.</summary>
    public string Pedido { get; set; } = string.Empty;
    /// <summary>Estado del pedido: Pagado, Enviado, Entregado...</summary>
    public string Estado { get; set; } = string.Empty;
    /// <summary>Estado de la carta suelta (Nueva, Casi nueva, Jugada) o vacío.</summary>
    public string Condicion { get; set; } = string.Empty;
    public string Cliente { get; set; } = string.Empty;
    /// <summary>True si es una preventa (reserva).</summary>
    public bool Reserva { get; set; }
}

/// <summary>Producto del inventario visto desde la app móvil.</summary>
public class ProductoMovilDto
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string Catalogo { get; set; } = string.Empty;
    public string Tipo { get; set; } = string.Empty;
    public string Condicion { get; set; } = string.Empty;
    public decimal Precio { get; set; }
    public int Stock { get; set; }
    public bool Preventa { get; set; }
}

/// <summary>Cuerpo de PUT /api/movil/productos/{id}/stock.</summary>
public class StockDto
{
    [Range(0, 9999, ErrorMessage = "El stock debe estar entre 0 y 9999.")]
    public int Stock { get; set; }
}

/// <summary>Cuerpo de PUT /api/movil/pedidos/{id}/estado. Valores: Pagado, Enviado, Entregado, Cancelado.</summary>
public class CambioEstadoDto
{
    [Required]
    public string Estado { get; set; } = string.Empty;
}

public record PedidoEstadoDto(int Id, string Numero, string Estado);
