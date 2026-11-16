using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using SaborTico.Api.Data;
using SaborTico.Api.Models;

namespace SaborTico.Api.Controllers;

/// <summary>Mantenimiento de usuarios: el administrador activa o desactiva el acceso móvil de otro (B3).</summary>
[Authorize(Roles = DbInicializador.RolAdministrador)]
public class UsuariosController : Controller
{
    private readonly UserManager<ApplicationUser> _usuarios;

    public UsuariosController(UserManager<ApplicationUser> usuarios) => _usuarios = usuarios;

    public async Task<IActionResult> Index()
    {
        var admins = await _usuarios.GetUsersInRoleAsync(DbInicializador.RolAdministrador);
        return View(admins.OrderBy(u => u.NombreCompleto).ToList());
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> CambiarAccesoMovil(string id)
    {
        var actual = await _usuarios.GetUserAsync(User);
        var usuario = await _usuarios.FindByIdAsync(id);
        if (usuario == null) return NotFound();
        if (actual?.Id == usuario.Id)
        {
            TempData["Mensaje"] = "No puede cambiar su propio acceso móvil.";
            return RedirectToAction(nameof(Index));
        }
        usuario.AccesoMovilHabilitado = !usuario.AccesoMovilHabilitado;
        await _usuarios.UpdateAsync(usuario);
        TempData["Mensaje"] = $"Acceso móvil de {usuario.NombreCompleto}: "
                              + (usuario.AccesoMovilHabilitado ? "habilitado." : "deshabilitado.");
        return RedirectToAction(nameof(Index));
    }
}
