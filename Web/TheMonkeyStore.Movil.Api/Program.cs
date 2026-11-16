using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using TheMonkeyStore.Movil.Api.Data;
using TheMonkeyStore.Movil.Api.Models;
using TheMonkeyStore.Movil.Api.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<ApplicationDbContext>(opciones =>
    opciones.UseSqlite(builder.Configuration.GetConnectionString("Default") ?? "Data Source=monkeystore_movil.db"));

builder.Services.AddIdentity<ApplicationUser, IdentityRole>()
    .AddEntityFrameworkStores<ApplicationDbContext>()
    .AddDefaultTokenProviders();

builder.Services.ConfigureApplicationCookie(o => o.LoginPath = "/Account/Login");

builder.Services.AddControllersWithViews();
builder.Services.AddOpenApi();
builder.Services.AddSingleton<IDescifradorMovil, DescifradorAesGcm>();
builder.Services.AddSingleton<ISesionesMovil, SesionesMovilEnMemoria>();

var app = builder.Build();

await DbInicializador.InicializarAsync(app.Services);

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi(); // documento OpenAPI en /openapi/v1.json
}

app.UseStaticFiles();
app.UseRouting();
app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();
app.MapControllerRoute(name: "default", pattern: "{controller=Home}/{action=Index}/{id?}");

app.Run();
