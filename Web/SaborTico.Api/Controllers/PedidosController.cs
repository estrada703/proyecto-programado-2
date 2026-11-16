using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SaborTico.Api.Data;

namespace SaborTico.Api.Controllers;

[Authorize(Roles = DbInicializador.RolAdministrador)]
public class PedidosController : Controller
{
    private readonly ApplicationDbContext _db;

    public PedidosController(ApplicationDbContext db) => _db = db;

    public async Task<IActionResult> Index()
    {
        var pedidos = await _db.Pedidos.AsNoTracking()
            .Include(p => p.Platillo).Include(p => p.Sede)
            .OrderByDescending(p => p.Fecha).ToListAsync();
        return View(pedidos);
    }
}
