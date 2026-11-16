using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using SaborTico.Api.Models;

namespace SaborTico.Api.Controllers;

public class LoginViewModel
{
    [Required(ErrorMessage = "Escriba su correo."), EmailAddress]
    public string Correo { get; set; } = string.Empty;

    [Required(ErrorMessage = "Escriba su contraseña."), DataType(DataType.Password)]
    public string Clave { get; set; } = string.Empty;
}

public class AccountController : Controller
{
    private readonly SignInManager<ApplicationUser> _signIn;

    public AccountController(SignInManager<ApplicationUser> signIn) => _signIn = signIn;

    [HttpGet]
    public IActionResult Login() => View(new LoginViewModel());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Login(LoginViewModel modelo)
    {
        if (!ModelState.IsValid) return View(modelo);
        var resultado = await _signIn.PasswordSignInAsync(modelo.Correo, modelo.Clave, false, false);
        if (!resultado.Succeeded)
        {
            ModelState.AddModelError(string.Empty, "Correo o contraseña incorrectos.");
            return View(modelo);
        }
        return RedirectToAction("Index", "Usuarios");
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Logout()
    {
        await _signIn.SignOutAsync();
        return RedirectToAction("Index", "Home");
    }
}
