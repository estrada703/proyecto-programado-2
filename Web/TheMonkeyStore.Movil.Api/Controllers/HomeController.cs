using Microsoft.AspNetCore.Mvc;

namespace TheMonkeyStore.Movil.Api.Controllers;

public class HomeController : Controller
{
    public IActionResult Index() => View();
}
