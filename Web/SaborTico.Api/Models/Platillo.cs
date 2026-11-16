namespace SaborTico.Api.Models;

/// <summary>El producto que el negocio vende o reserva.</summary>
public class Platillo
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string Descripcion { get; set; } = string.Empty;
    public decimal Precio { get; set; }
}
