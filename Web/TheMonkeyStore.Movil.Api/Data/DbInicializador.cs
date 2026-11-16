using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using TheMonkeyStore.Movil.Api.Models;

namespace TheMonkeyStore.Movil.Api.Data;

/// <summary>Crea la base de datos y los datos de prueba de The Monkey Store (idempotente).</summary>
public static class DbInicializador
{
    public const string RolAdministrador = "Administrador";

    // Credenciales de prueba, documentadas en el LEEME.md.
    public const string CorreoAdminMovil = "admin@themonkeystore.test";
    public const string CorreoAdminSinMovil = "admin.sinmovil@themonkeystore.test";
    public const string ClavePrueba = "Admin123!";

    public static async Task InicializarAsync(IServiceProvider servicios)
    {
        using var alcance = servicios.CreateScope();
        var db = alcance.ServiceProvider.GetRequiredService<ApplicationDbContext>();

        if (db.Database.GetMigrations().Any())
        {
            await db.Database.MigrateAsync();
        }
        else
        {
            await db.Database.EnsureCreatedAsync();
            try
            {
                // Base de pruebas de una versión anterior (sin la columna Stock): se recrea.
                await db.Productos.Select(p => p.Stock).FirstOrDefaultAsync();
            }
            catch (Microsoft.Data.Sqlite.SqliteException)
            {
                await db.Database.EnsureDeletedAsync();
                await db.Database.EnsureCreatedAsync();
            }
        }

        var roles = alcance.ServiceProvider.GetRequiredService<RoleManager<IdentityRole>>();
        if (!await roles.RoleExistsAsync(RolAdministrador))
            await roles.CreateAsync(new IdentityRole(RolAdministrador));

        var usuarios = alcance.ServiceProvider.GetRequiredService<UserManager<ApplicationUser>>();
        await CrearAdminAsync(usuarios, CorreoAdminMovil, "Gabriel Estrada", accesoMovil: true);
        await CrearAdminAsync(usuarios, CorreoAdminSinMovil, "Admin Sin Móvil", accesoMovil: false);

        if (await db.Franquicias.AnyAsync()) return;

        var pokemon = new Franquicia { Nombre = "Pokémon TCG", Tipo = TipoFranquicia.JuegoDeCartas };
        var magic = new Franquicia { Nombre = "Magic: The Gathering", Tipo = TipoFranquicia.JuegoDeCartas };
        var yugioh = new Franquicia { Nombre = "Yu-Gi-Oh!", Tipo = TipoFranquicia.JuegoDeCartas };
        var onePiece = new Franquicia { Nombre = "One Piece Card Game", Tipo = TipoFranquicia.JuegoDeCartas };
        var lorcana = new Franquicia { Nombre = "Lorcana", Tipo = TipoFranquicia.JuegoDeCartas };
        db.Franquicias.AddRange(pokemon, magic, yugioh, onePiece, lorcana);

        Producto P(Franquicia f, string nombre, string desc, TipoProducto tipo, decimal precio,
                   EstadoCarta? estado = null, bool preventa = false) =>
            new() { Franquicia = f, Nombre = nombre, Descripcion = desc, Tipo = tipo,
                    PrecioBase = precio, Estado = estado, EsPreventa = preventa };

        var productos = new[]
        {
            P(pokemon, "Charizard ex", "Carta suelta ilustración especial, edición Obsidian Flames.", TipoProducto.CartaSuelta, 48000, EstadoCarta.Nueva),
            P(pokemon, "Charizard ex", "Carta suelta ilustración especial, edición Obsidian Flames.", TipoProducto.CartaSuelta, 48000, EstadoCarta.Jugada),
            P(pokemon, "Sobre Escarlata y Púrpura", "Sobre de 10 cartas de la expansión base.", TipoProducto.Sobre, 3200),
            P(pokemon, "Elite Trainer Box Prismatic", "Caja de entrenador élite con 9 sobres y accesorios.", TipoProducto.Caja, 39500, preventa: true),
            P(magic, "Booster de Colección Bloomburrow", "Sobre de colección con 15 cartas y al menos una foil.", TipoProducto.Sobre, 9800),
            P(magic, "Caja de Draft Duskmourn", "Caja con 36 sobres de draft.", TipoProducto.Caja, 98000),
            P(magic, "The One Ring", "Carta suelta mítica, edición El Señor de los Anillos.", TipoProducto.CartaSuelta, 62000, EstadoCarta.CasiNueva),
            P(yugioh, "Ash Blossom & Joyous Spring", "Carta suelta, rareza ultra rara.", TipoProducto.CartaSuelta, 7500, EstadoCarta.Nueva),
            P(yugioh, "Sobre 25th Anniversary", "Sobre aniversario con 9 cartas.", TipoProducto.Sobre, 2900),
            P(onePiece, "Booster Box OP-09", "Caja de 24 sobres de la expansión OP-09.", TipoProducto.Caja, 72000, preventa: true),
            P(onePiece, "Monkey D. Luffy (Alt Art)", "Carta suelta arte alternativo.", TipoProducto.CartaSuelta, 35000, EstadoCarta.Nueva),
            P(lorcana, "Starter Deck Azurite Sea", "Mazo de inicio listo para jugar.", TipoProducto.Caja, 12500),
            P(lorcana, "Fundas premium Dragon Shield", "Paquete de 100 fundas mate.", TipoProducto.Accesorio, 6500),
        };
        var stocks = new[] { 3, 1, 48, 0, 20, 4, 1, 6, 60, 2, 2, 9, 25 };
        for (var i = 0; i < productos.Length; i++) productos[i].Stock = stocks[i];
        db.Productos.AddRange(productos);
        await db.SaveChangesAsync();

        var clientes = new[] { "María Rodríguez", "José Vargas", "Daniela Mora", "Luis Jiménez", "Valeria Solís", "Andrés Quesada" };
        var estados = new[] { EstadoPedido.Entregado, EstadoPedido.Enviado, EstadoPedido.Pagado, EstadoPedido.Pagado, EstadoPedido.PendientePago, EstadoPedido.Cancelado };
        var azar = new Random(703); // semilla fija: los mismos datos en cada máquina
        var inicio = new DateTime(2026, 10, 20, 10, 0, 0);
        for (var i = 0; i < 22; i++)
        {
            var pedido = new Pedido
            {
                NombreCliente = clientes[i % clientes.Length],
                Fecha = inicio.AddDays(i * 1.2).AddHours(azar.Next(0, 9)).AddMinutes(azar.Next(0, 60)),
                Estado = i < 4 ? EstadoPedido.Pagado : estados[azar.Next(estados.Length)],
            };
            var lineas = azar.Next(1, 4);
            foreach (var producto in productos.OrderBy(_ => azar.Next()).Take(lineas))
            {
                pedido.Detalles.Add(new DetallePedido
                {
                    Producto = producto,
                    Cantidad = producto.Tipo == TipoProducto.Sobre ? azar.Next(1, 7) : azar.Next(1, 3),
                    PrecioUnitario = producto.PrecioFinal,
                });
            }
            db.Pedidos.Add(pedido);
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
