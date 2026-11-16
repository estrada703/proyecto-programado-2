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
        var (_, error) = await UsuarioDeSesionAsync();
        if (error != null) return error;

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
                PedidoId = d.PedidoId,
                Pedido = d.Pedido.Numero,
                Estado = d.Pedido.Estado.Mostrar(),
                Condicion = d.Producto.Estado?.Mostrar() ?? string.Empty,
                Cliente = d.Pedido.NombreCliente,
                Reserva = d.Producto.EsPreventa,
            });
        return Ok(lista);
    }

    /// <summary>Inventario: todos los productos con su stock, los agotados primero.</summary>
    /// <response code="200">Listado de productos.</response>
    /// <response code="401">Sin token, token inválido o vencido.</response>
    /// <response code="403">El administrador ya no tiene el acceso móvil habilitado.</response>
    [HttpGet("productos")]
    [ProducesResponseType(typeof(IEnumerable<ProductoMovilDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    public async Task<ActionResult<IEnumerable<ProductoMovilDto>>> Productos()
    {
        var (_, error) = await UsuarioDeSesionAsync();
        if (error != null) return error;

        var productos = await _db.Productos.AsNoTracking().Include(p => p.Franquicia).ToListAsync();
        return Ok(productos
            .OrderBy(p => p.Stock).ThenBy(p => p.Nombre)
            .Select(AProductoDto));
    }

    /// <summary>Actualiza el stock de un producto.</summary>
    /// <response code="200">Stock actualizado; devuelve el producto.</response>
    /// <response code="400">Stock fuera de rango (0 a 9999).</response>
    /// <response code="401">Sin token, token inválido o vencido.</response>
    /// <response code="403">Sin acceso móvil.</response>
    /// <response code="404">El producto no existe.</response>
    [HttpPut("productos/{id:int}/stock")]
    [ProducesResponseType(typeof(ProductoMovilDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<ProductoMovilDto>> ActualizarStock(int id, [FromBody] StockDto dto)
    {
        var (usuario, error) = await UsuarioDeSesionAsync();
        if (error != null) return error;

        var producto = await _db.Productos.Include(p => p.Franquicia).FirstOrDefaultAsync(p => p.Id == id);
        if (producto == null) return NotFound();
        var anterior = producto.Stock;
        producto.Stock = dto.Stock;
        await _db.SaveChangesAsync();
        _log.LogInformation("Usuario {UsuarioId} cambió el stock del producto {ProductoId}: {Anterior} -> {Nuevo}",
            usuario!.Id, id, anterior, dto.Stock);
        return Ok(AProductoDto(producto));
    }

    /// <summary>Cambia el estado de un pedido. Al cancelarlo, el stock de sus productos se devuelve.</summary>
    /// <response code="200">Estado actualizado.</response>
    /// <response code="400">Estado desconocido.</response>
    /// <response code="401">Sin token, token inválido o vencido.</response>
    /// <response code="403">Sin acceso móvil.</response>
    /// <response code="404">El pedido no existe.</response>
    /// <response code="409">El cambio no está permitido desde el estado actual.</response>
    [HttpPut("pedidos/{id:int}/estado")]
    [ProducesResponseType(typeof(PedidoEstadoDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status409Conflict)]
    public async Task<ActionResult<PedidoEstadoDto>> CambiarEstado(int id, [FromBody] CambioEstadoDto dto)
    {
        var (usuario, error) = await UsuarioDeSesionAsync();
        if (error != null) return error;

        if (!Enum.TryParse<EstadoPedido>(dto.Estado, ignoreCase: true, out var nuevo)
            || !Enum.IsDefined(nuevo))
            return BadRequest(new ProblemDetails { Title = "Estado desconocido", Status = 400 });

        var pedido = await _db.Pedidos.Include(p => p.Detalles).ThenInclude(d => d.Producto)
            .FirstOrDefaultAsync(p => p.Id == id);
        if (pedido == null) return NotFound();
        if (!TransicionesPedido.EsPermitida(pedido.Estado, nuevo))
            return Conflict(new ProblemDetails { Title = "Cambio de estado no permitido", Status = 409 });

        if (nuevo == EstadoPedido.Cancelado)
        {
            foreach (var d in pedido.Detalles)
                d.Producto!.Stock += d.Cantidad;
        }
        var anterior = pedido.Estado;
        pedido.Estado = nuevo;
        await _db.SaveChangesAsync();
        _log.LogInformation("Usuario {UsuarioId} cambió el pedido {Numero}: {Anterior} -> {Nuevo}",
            usuario!.Id, pedido.Numero, anterior, nuevo);
        return Ok(new PedidoEstadoDto(pedido.Id, pedido.Numero, pedido.Estado.Mostrar()));
    }

    private static ProductoMovilDto AProductoDto(Producto p) => new()
    {
        Id = p.Id,
        Nombre = p.Nombre,
        Catalogo = p.Franquicia?.Nombre ?? string.Empty,
        Tipo = p.Tipo.Mostrar(),
        Condicion = p.Estado?.Mostrar() ?? string.Empty,
        Precio = p.PrecioFinal,
        Stock = p.Stock,
        Preventa = p.EsPreventa,
    };

    /// <summary>Valida el token Bearer: 401 si falta o venció, 403 si el usuario ya no tiene acceso móvil.</summary>
    private async Task<(ApplicationUser? Usuario, ActionResult? Error)> UsuarioDeSesionAsync()
    {
        var token = ObtenerToken();
        var sesion = token == null ? null : _sesiones.Obtener(token);
        if (sesion == null) return (null, Unauthorized());
        var usuario = await _usuarios.FindByIdAsync(sesion.UsuarioId);
        if (usuario == null) return (null, Unauthorized());
        if (!usuario.AccesoMovilHabilitado) return (null, StatusCode(StatusCodes.Status403Forbidden));
        return (usuario, null);
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
