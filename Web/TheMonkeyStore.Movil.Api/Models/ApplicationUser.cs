using Microsoft.AspNetCore.Identity;

namespace TheMonkeyStore.Movil.Api.Models;

/// <summary>Usuario del sistema. Los administradores pueden tener o no el acceso móvil.</summary>
public class ApplicationUser : IdentityUser
{
    public string NombreCompleto { get; set; } = string.Empty;

    /// <summary>B3: lo activa o desactiva otro administrador desde la aplicación web.</summary>
    public bool AccesoMovilHabilitado { get; set; }
}
