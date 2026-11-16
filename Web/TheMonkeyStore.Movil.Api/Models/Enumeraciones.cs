using System.ComponentModel.DataAnnotations;

namespace TheMonkeyStore.Movil.Api.Models;

/// <summary>Tipo de franquicia: juego de cartas coleccionables o consola (igual que en el PP1).</summary>
public enum TipoFranquicia
{
    [Display(Name = "Juego de cartas")] JuegoDeCartas,
    [Display(Name = "Consola")] Consola
}

/// <summary>Tipo de producto que se vende en la tienda.</summary>
public enum TipoProducto
{
    [Display(Name = "Sobre")] Sobre,
    [Display(Name = "Caja")] Caja,
    [Display(Name = "Carta suelta")] CartaSuelta,
    [Display(Name = "Juego")] Juego,
    [Display(Name = "Accesorio")] Accesorio
}

/// <summary>Estado físico de una carta suelta.</summary>
public enum EstadoCarta
{
    [Display(Name = "Nueva")] Nueva,
    [Display(Name = "Casi nueva")] CasiNueva,
    [Display(Name = "Jugada")] Jugada
}

/// <summary>Ciclo de vida de un pedido.</summary>
public enum EstadoPedido
{
    [Display(Name = "Pendiente de pago")] PendientePago,
    [Display(Name = "Pagado")] Pagado,
    [Display(Name = "Enviado")] Enviado,
    [Display(Name = "Entregado")] Entregado,
    [Display(Name = "Cancelado")] Cancelado
}

public static class TextoEnum
{
    /// <summary>Nombre para mostrar de un valor de enumeración ([Display(Name)]).</summary>
    public static string Mostrar(this Enum valor)
    {
        var miembro = valor.GetType().GetMember(valor.ToString()).FirstOrDefault();
        var atributo = miembro?.GetCustomAttributes(typeof(DisplayAttribute), false)
            .OfType<DisplayAttribute>().FirstOrDefault();
        return atributo?.Name ?? valor.ToString();
    }
}
