using System.Security.Cryptography;
using System.Text;

namespace SaborTico.Api.Services;

public interface IDescifradorMovil
{
    /// <summary>Descifra Base64(IV(12) || cifrado || etiqueta(16)). Un mensaje alterado lanza CryptographicException.</summary>
    string Descifrar(string datos);
}

/// <summary>AES-256-GCM con la clave de user secrets (Movil:ClaveAes, 32 bytes en Base64).</summary>
public class DescifradorAesGcm : IDescifradorMovil
{
    private const int LongitudIv = 12;
    private const int LongitudEtiqueta = 16;
    private readonly byte[] _clave;

    public DescifradorAesGcm(IConfiguration configuracion)
    {
        var base64 = configuracion["Movil:ClaveAes"];
        if (string.IsNullOrWhiteSpace(base64))
            throw new InvalidOperationException(
                "Falta Movil:ClaveAes. Use: dotnet user-secrets set \"Movil:ClaveAes\" \"<clave en Base64>\"");
        _clave = Convert.FromBase64String(base64);
        if (_clave.Length != 32)
            throw new InvalidOperationException("Movil:ClaveAes debe ser de 32 bytes (AES-256) en Base64.");
    }

    public string Descifrar(string datos)
    {
        var paquete = Convert.FromBase64String(datos);
        if (paquete.Length < LongitudIv + LongitudEtiqueta)
            throw new CryptographicException("Mensaje demasiado corto.");

        var iv = paquete.AsSpan(0, LongitudIv);
        var etiqueta = paquete.AsSpan(paquete.Length - LongitudEtiqueta, LongitudEtiqueta);
        var cifrado = paquete.AsSpan(LongitudIv, paquete.Length - LongitudIv - LongitudEtiqueta);
        var plano = new byte[cifrado.Length];

        using var aes = new AesGcm(_clave, LongitudEtiqueta);
        aes.Decrypt(iv, cifrado, etiqueta, plano); // si el mensaje fue alterado, lanza una excepción
        return Encoding.UTF8.GetString(plano);
    }
}
