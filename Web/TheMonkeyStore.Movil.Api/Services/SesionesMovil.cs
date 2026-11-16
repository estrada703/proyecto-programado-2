using System.Collections.Concurrent;

namespace TheMonkeyStore.Movil.Api.Services;

public record SesionMovil(string UsuarioId, DateTime Vence);

public interface ISesionesMovil
{
    string Crear(string usuarioId);
    SesionMovil? Obtener(string token);
}

/// <summary>Tokens de sesión móvil guardados en memoria con su usuario y vencimiento.</summary>
public class SesionesMovilEnMemoria : ISesionesMovil
{
    private static readonly TimeSpan Duracion = TimeSpan.FromHours(2);
    private readonly ConcurrentDictionary<string, SesionMovil> _sesiones = new();

    public string Crear(string usuarioId)
    {
        var token = Guid.NewGuid().ToString("N");
        _sesiones[token] = new SesionMovil(usuarioId, DateTime.UtcNow.Add(Duracion));
        return token;
    }

    public SesionMovil? Obtener(string token)
    {
        if (!_sesiones.TryGetValue(token, out var sesion)) return null;
        if (sesion.Vence <= DateTime.UtcNow)
        {
            _sesiones.TryRemove(token, out _);
            return null;
        }
        return sesion;
    }
}
