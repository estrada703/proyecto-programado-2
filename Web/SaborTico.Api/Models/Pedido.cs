using System.ComponentModel.DataAnnotations.Schema;

namespace SaborTico.Api.Models;

/// <summary>Compra o reserva de un platillo en una sede.</summary>
public class Pedido
{
    public int Id { get; set; }
    public int PlatilloId { get; set; }
    public Platillo? Platillo { get; set; }
    public int SedeId { get; set; }
    public Sede? Sede { get; set; }
    public int Cantidad { get; set; }
    public DateTime Fecha { get; set; }

    /// <summary>"Compra" o "Reserva".</summary>
    public string Tipo { get; set; } = "Compra";

    /// <summary>El cálculo vive en el modelo, no en el controlador ni en la vista.</summary>
    [NotMapped]
    public decimal Monto => Cantidad * (Platillo?.Precio ?? 0m);
}
