using System.ComponentModel.DataAnnotations.Schema;

namespace TheMonkeyStore.Movil.Api.Models;

/// <summary>Catálogo 1: franquicia (Pokémon TCG, Magic: The Gathering, etc.).</summary>
public class Franquicia
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public TipoFranquicia Tipo { get; set; }
    public bool Activa { get; set; } = true;
    public ICollection<Producto> Productos { get; set; } = new List<Producto>();
}

/// <summary>Producto a la venta. El precio de una carta suelta depende de su estado.</summary>
public class Producto
{
    public const decimal FactorCasiNueva = 0.85m;
    public const decimal FactorJugada = 0.60m;

    public int Id { get; set; }
    public int FranquiciaId { get; set; }
    public Franquicia? Franquicia { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string Descripcion { get; set; } = string.Empty;
    public TipoProducto Tipo { get; set; }
    public EstadoCarta? Estado { get; set; }
    public decimal PrecioBase { get; set; }
    public bool EsPreventa { get; set; }

    /// <summary>Precio final según el estado de la carta (la regla vive en el modelo).</summary>
    [NotMapped]
    public decimal PrecioFinal => Estado switch
    {
        EstadoCarta.CasiNueva => Math.Round(PrecioBase * FactorCasiNueva, 0),
        EstadoCarta.Jugada => Math.Round(PrecioBase * FactorJugada, 0),
        _ => PrecioBase
    };
}

/// <summary>Pedido de un cliente.</summary>
public class Pedido
{
    public int Id { get; set; }
    public string NombreCliente { get; set; } = string.Empty;
    public DateTime Fecha { get; set; }
    public EstadoPedido Estado { get; set; }
    public ICollection<DetallePedido> Detalles { get; set; } = new List<DetallePedido>();

    /// <summary>Número de pedido para mostrar (igual que en el PP1).</summary>
    [NotMapped]
    public string Numero => $"TMS-{Id:D6}";
}

/// <summary>Línea de un pedido: el "producto comprado o reservado" que ve la app móvil.</summary>
public class DetallePedido
{
    public int Id { get; set; }
    public int PedidoId { get; set; }
    public Pedido? Pedido { get; set; }
    public int ProductoId { get; set; }
    public Producto? Producto { get; set; }
    public int Cantidad { get; set; }

    /// <summary>Precio unitario cobrado al momento de la compra.</summary>
    public decimal PrecioUnitario { get; set; }

    [NotMapped]
    public decimal Subtotal => PrecioUnitario * Cantidad;
}
