using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TheMonkeyStore.Movil.Api.Data;

namespace TheMonkeyStore.Movil.Api.Controllers;

[Authorize(Roles = DbInicializador.RolAdministrador)]
public class PedidosController : Controller
{
    private readonly ApplicationDbContext _db;

    public PedidosController(ApplicationDbContext db) => _db = db;

    public async Task<IActionResult> Index()
    {
        var pedidos = await _db.Pedidos.AsNoTracking()
            .Include(p => p.Detalles).ThenInclude(d => d.Producto).ThenInclude(p => p!.Franquicia)
            .ToListAsync();
        return View(pedidos.OrderByDescending(p => p.Fecha).ToList());
    }
}
