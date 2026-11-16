using Microsoft.AspNetCore.Mvc;

namespace SaborTico.Api.Controllers;

public class HomeController : Controller
{
    public IActionResult Index() => View();
}
