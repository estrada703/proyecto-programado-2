using Microsoft.AspNetCore.Identity;
using System.Security.Cryptography;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TheMonkeyStore.Movil.Api.Data;
using TheMonkeyStore.Movil.Api.Dtos;
using TheMonkeyStore.Movil.Api.Models;
using TheMonkeyStore.Movil.Api.Services;

namespace TheMonkeyStore.Movil.Api.Controllers;

/// <summary>Operaciones de la aplicación móvil para administradores.</summary>
[ApiController]
[Route("api/movil")]
[Produces("application/json")]
public class MovilApiController : ControllerBase
{
    private static readonly JsonSerializerOptions OpcionesJson = new() { PropertyNameCaseInsensitive = true };

    private readonly UserManager<ApplicationUser> _usuarios;
    private readonly ApplicationDbContext _db;
    private readonly IDescifradorMovil _descifrador;
    private readonly ISesionesMovil _sesiones;
    private readonly ILogger<MovilApiController> _log;

    public MovilApiController(UserManager<ApplicationUser> usuarios, ApplicationDbContext db,
        IDescifradorMovil descifrador, ISesionesMovil sesiones, ILogger<MovilApiController> log)
    {
        _usuarios = usuarios;
        _db = db;
        _descifrador = descifrador;
        _sesiones = sesiones;
        _log = log;
    }

    /// <summary>Indica si las credenciales cifradas pertenecen a un administrador válido (registro en el dispositivo).</summary>
    /// <response code="200">Es un administrador válido.</response>
    /// <response code="400">Mensaje alterado o mal formado.</response>
    /// <response code="401">Credenciales inválidas.</response>
    /// <response code="403">Administrador sin acceso móvil habilitado.</response>
    [HttpPost("validar")]
    [ProducesResponseType(typeof(ValidarRespuestaDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    public async Task<ActionResult<ValidarRespuestaDto>> Validar([FromBody] DatosCifradosDto dto)
    {
        var (usuario, error) = await AutenticarAsync(dto);
        if (error != null) return error;
        return Ok(new ValidarRespuestaDto(true, usuario!.NombreCompleto));
    }

    /// <summary>Ingreso del administrador: devuelve su nombre y un token de sesión.</summary>
    /// <response code="200">Ingreso aceptado.</response>
    /// <response code="400">Mensaje alterado o mal formado.</response>
    /// <response code="401">Credenciales inválidas.</response>
    /// <response code="403">Administrador sin acceso móvil habilitado.</response>
    [HttpPost("ingresar")]
    [ProducesResponseType(typeof(IngresoRespuestaDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    public async Task<ActionResult<IngresoRespuestaDto>> Ingresar([FromBody] DatosCifradosDto dto)
    {
        var (usuario, error) = await AutenticarAsync(dto);
        if (error != null) return error;
        var token = _sesiones.Crear(usuario!.Id);
        _log.LogInformation("Ingreso móvil aceptado para el usuario {UsuarioId}", usuario.Id);
        return Ok(new IngresoRespuestaDto(usuario.NombreCompleto, token));
    }

    /// <summary>Productos comprados o reservados (líneas de pedido), de la más reciente a la más antigua.</summary>
    /// <remarks>Requiere el encabezado <c>Authorization: Bearer (token)</c>.</remarks>
    /// <response code="200">Listado (puede ser vacío).</response>
    /// <response code="401">Sin token, token inválido o vencido.</response>
    /// <response code="403">El administrador ya no tiene el acceso móvil habilitado.</response>
    [HttpGet("compras")]
    [ProducesResponseType(typeof(IEnumerable<CompraMovilDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    public async Task<ActionResult<IEnumerable<CompraMovilDto>>> Compras()
    {
        var token = ObtenerToken();
        var sesion = token == null ? null : _sesiones.Obtener(token);
        if (sesion == null) return Unauthorized();

        var usuario = await _usuarios.FindByIdAsync(sesion.UsuarioId);
        if (usuario == null) return Unauthorized();
        if (!usuario.AccesoMovilHabilitado) return StatusCode(StatusCodes.Status403Forbidden);

        var detalles = await _db.DetallesPedido.AsNoTracking()
            .Include(d => d.Pedido)
            .Include(d => d.Producto).ThenInclude(p => p!.Franquicia)
            .ToListAsync();

        var lista = detalles
            .OrderByDescending(d => d.Pedido!.Fecha)
            .Select(d => new CompraMovilDto
            {
                Id = d.Id,
                Producto = d.Producto!.Nombre,
                Descripcion = d.Producto.Descripcion,
                Precio = d.PrecioUnitario,
                Cantidad = d.Cantidad,
                Fecha = d.Pedido!.Fecha,
                Monto = d.Subtotal,
                Catalogo = d.Producto.Franquicia!.Nombre,
                Tipo = d.Producto.Tipo.Mostrar(),
                Pedido = d.Pedido.Numero,
                Estado = d.Pedido.Estado.Mostrar(),
                Condicion = d.Producto.Estado?.Mostrar() ?? string.Empty,
                Cliente = d.Pedido.NombreCliente,
                Reserva = d.Producto.EsPreventa,
            });
        return Ok(lista);
    }

    private string? ObtenerToken()
    {
        var encabezado = Request.Headers.Authorization.ToString();
        const string prefijo = "Bearer ";
        return encabezado.StartsWith(prefijo, StringComparison.OrdinalIgnoreCase)
            ? encabezado[prefijo.Length..].Trim()
            : null;
    }

    /// <summary>Descifra y valida: 400 mensaje alterado, 401 credenciales o rol inválidos, 403 sin acceso móvil.</summary>
    private async Task<(ApplicationUser? Usuario, ActionResult? Error)> AutenticarAsync(DatosCifradosDto dto)
    {
        CredencialesDto? credenciales;
        try
        {
            var json = _descifrador.Descifrar(dto.Datos);
            credenciales = JsonSerializer.Deserialize<CredencialesDto>(json, OpcionesJson);
        }
        catch (Exception ex) when (ex is FormatException or CryptographicException or JsonException)
        {
            // No se revelan detalles al cliente; solo queda constancia en la bitácora del servidor.
            _log.LogWarning("Mensaje cifrado rechazado: {Tipo}", ex.GetType().Name);
            return (null, BadRequest(new ProblemDetails { Title = "Solicitud inválida", Status = 400 }));
        }

        if (credenciales == null || string.IsNullOrWhiteSpace(credenciales.Correo)
            || string.IsNullOrEmpty(credenciales.Clave))
            return (null, BadRequest(new ProblemDetails { Title = "Solicitud inválida", Status = 400 }));

        var usuario = await _usuarios.FindByEmailAsync(credenciales.Correo.Trim());
        if (usuario == null || !await _usuarios.CheckPasswordAsync(usuario, credenciales.Clave)
            || !await _usuarios.IsInRoleAsync(usuario, DbInicializador.RolAdministrador))
        {
            _log.LogWarning("Credenciales móviles inválidas");
            return (null, Unauthorized());
        }

        if (!usuario.AccesoMovilHabilitado)
        {
            _log.LogWarning("Administrador {UsuarioId} sin acceso móvil", usuario.Id);
            return (null, StatusCode(StatusCodes.Status403Forbidden));
        }

        return (usuario, null);
    }
}
