namespace SaborTico.Api.Models;

/// <summary>Catálogo 1 del negocio: las sedes del restaurante.</summary>
public class Sede
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string Direccion { get; set; } = string.Empty;
}
