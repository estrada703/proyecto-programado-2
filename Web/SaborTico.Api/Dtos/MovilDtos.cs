using System.ComponentModel.DataAnnotations;

namespace SaborTico.Api.Dtos;

/// <summary>Cuerpo de los endpoints POST: { "datos": "&lt;Base64(IV || cifrado || etiqueta)&gt;" }.</summary>
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
    public string Catalogo { get; set; } = string.Empty;
    public string Tipo { get; set; } = string.Empty;
}
