using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using TheMonkeyStore.Movil.Api.Models;

namespace TheMonkeyStore.Movil.Api.Data;

public class ApplicationDbContext : IdentityDbContext<ApplicationUser>
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options) { }

    public DbSet<Franquicia> Franquicias => Set<Franquicia>();
    public DbSet<Producto> Productos => Set<Producto>();
    public DbSet<Pedido> Pedidos => Set<Pedido>();
    public DbSet<DetallePedido> DetallesPedido => Set<DetallePedido>();

    protected override void OnModelCreating(ModelBuilder modelo)
    {
        base.OnModelCreating(modelo);
        // SQLite no ordena decimal de forma nativa: se guarda como double.
        modelo.Entity<Producto>().Property(p => p.PrecioBase).HasConversion<double>();
        modelo.Entity<DetallePedido>().Property(d => d.PrecioUnitario).HasConversion<double>();
    }
}
