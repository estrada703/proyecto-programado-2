using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using SaborTico.Api.Models;

namespace SaborTico.Api.Data;

/// <summary>Crea la base de datos y los datos de prueba (idempotente).</summary>
public static class DbInicializador
{
    public const string RolAdministrador = "Administrador";

    // Credenciales de prueba, documentadas en el LEEME.md.
    public const string CorreoAdminMovil = "admin@sabortico.cr";
    public const string CorreoAdminSinMovil = "admin.sinmovil@sabortico.cr";
    public const string ClavePrueba = "Admin123!";

    public static async Task InicializarAsync(IServiceProvider servicios)
    {
        using var alcance = servicios.CreateScope();
        var db = alcance.ServiceProvider.GetRequiredService<ApplicationDbContext>();

        // Si ya existen migraciones (dotnet ef migrations add) se aplican; si no, se crea el esquema.
        if (db.Database.GetMigrations().Any())
            await db.Database.MigrateAsync();
        else
            await db.Database.EnsureCreatedAsync();

        var roles = alcance.ServiceProvider.GetRequiredService<RoleManager<IdentityRole>>();
        if (!await roles.RoleExistsAsync(RolAdministrador))
            await roles.CreateAsync(new IdentityRole(RolAdministrador));

        var usuarios = alcance.ServiceProvider.GetRequiredService<UserManager<ApplicationUser>>();
        await CrearAdminAsync(usuarios, CorreoAdminMovil, "Ana Administradora", accesoMovil: true);
        await CrearAdminAsync(usuarios, CorreoAdminSinMovil, "Beto Sin Móvil", accesoMovil: false);

        if (await db.Sedes.AnyAsync()) return;

        var sedes = new[]
        {
            new Sede { Nombre = "Sede San José", Direccion = "Avenida Central, San José" },
            new Sede { Nombre = "Sede Cartago", Direccion = "Frente a las Ruinas, Cartago" },
            new Sede { Nombre = "Sede Heredia", Direccion = "Costado norte del parque, Heredia" },
        };
        db.Sedes.AddRange(sedes);

        var platillos = new[]
        {
            new Platillo { Nombre = "Casado con pollo", Descripcion = "Arroz, frijoles, ensalada, plátano maduro y pollo a la plancha.", Precio = 3500m },
            new Platillo { Nombre = "Gallo pinto especial", Descripcion = "Gallo pinto con huevo, natilla y tortilla palmeada.", Precio = 2200m },
            new Platillo { Nombre = "Olla de carne", Descripcion = "Sopa tradicional con yuca, ñampí, chayote y carne de res.", Precio = 4200m },
            new Platillo { Nombre = "Arroz con camarones", Descripcion = "Arroz salteado con camarones y vegetales.", Precio = 5800m },
            new Platillo { Nombre = "Chifrijo", Descripcion = "Arroz, frijoles, chicharrón y pico de gallo.", Precio = 3900m },
            new Platillo { Nombre = "Tres leches", Descripcion = "Postre de la casa.", Precio = 1800m },
        };
        db.Platillos.AddRange(platillos);
        await db.SaveChangesAsync();

        var baseFecha = new DateTime(2026, 11, 1, 12, 0, 0);
        var datos = new (int plat, int sede, int cant, int dias, string tipo)[]
        {
            (0, 0, 2, 0, "Compra"), (1, 1, 1, 1, "Compra"), (2, 2, 3, 2, "Reserva"),
            (3, 0, 2, 3, "Compra"), (4, 1, 4, 4, "Compra"), (5, 2, 2, 5, "Compra"),
            (0, 1, 1, 6, "Reserva"), (2, 0, 2, 7, "Compra"), (4, 2, 3, 8, "Reserva"),
            (3, 1, 1, 9, "Compra"), (1, 0, 2, 10, "Compra"), (5, 1, 5, 11, "Reserva"),
        };
        foreach (var d in datos)
        {
            db.Pedidos.Add(new Pedido
            {
                PlatilloId = platillos[d.plat].Id,
                SedeId = sedes[d.sede].Id,
                Cantidad = d.cant,
                Fecha = baseFecha.AddDays(d.dias).AddHours(d.dias % 5),
                Tipo = d.tipo,
            });
        }
        await db.SaveChangesAsync();
    }

    private static async Task CrearAdminAsync(UserManager<ApplicationUser> usuarios, string correo,
                                              string nombre, bool accesoMovil)
    {
        if (await usuarios.FindByEmailAsync(correo) != null) return;
        var usuario = new ApplicationUser
        {
            UserName = correo,
            Email = correo,
            EmailConfirmed = true,
            NombreCompleto = nombre,
            AccesoMovilHabilitado = accesoMovil,
        };
        var resultado = await usuarios.CreateAsync(usuario, ClavePrueba);
        if (resultado.Succeeded)
            await usuarios.AddToRoleAsync(usuario, RolAdministrador);
    }
}
