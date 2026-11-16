# Proyecto Programado 2 — Aplicación móvil para administradores (Sabor Tico)

Aplicación Android nativa en Java que consulta la Web API del Proyecto Programado 1.
El dominio es un restaurante: el producto son los platillos y el catálogo 1 son las sedes.

```
Android/   proyecto de Android Studio (Java, minSdk 24)
Web/       solución ASP.NET Core con la Web API y la aplicación web
APK/       app-release.apk firmado (se genera con Build > Generate Signed Bundle / APK)
local.properties.ejemplo   claves que la aplicación espera, con una clave AES de prueba
```

## 1. Ejecutar la Web API

Requisitos: .NET 10 SDK.

```bash
cd Web/SaborTico.Api
dotnet user-secrets set "Movil:ClaveAes" "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
dotnet run --launch-profile http
```

La API queda en `http://localhost:5080` (desde el emulador: `http://10.0.2.2:5080/`).
La base de datos SQLite (`sabortico.db`) y los datos de prueba se crean al primer arranque.
La documentación OpenAPI está en `http://localhost:5080/openapi/v1.json`.

**Migración (B3).** Para registrar la columna `AccesoMovilHabilitado` como migración de EF Core:

```bash
dotnet tool install --global dotnet-ef
dotnet ef migrations add AccesoMovil
```

Con migraciones presentes, el arranque las aplica; sin ellas, crea el esquema directamente.

La clave AES de este archivo es **de prueba**. Debe ser idéntica a `aes.key.base64` de la aplicación.

## 2. Administradores de prueba

| Correo | Contraseña | Acceso móvil |
|---|---|---|
| admin@sabortico.cr | Admin123! | Habilitado |
| admin.sinmovil@sabortico.cr | Admin123! | Deshabilitado (demuestra el 403 de F1) |

Desde la aplicación web (`/Account/Login`, luego **Usuarios**) un administrador activa o desactiva
el acceso móvil del otro (B3).

## 3. Ejecutar la aplicación Android

1. Copie `local.properties.ejemplo` como `Android/local.properties` (Android Studio agrega `sdk.dir`).
2. Abra la carpeta `Android/` en Android Studio y sincronice Gradle.
3. Inicie un emulador (API 24 o superior) y ejecute `app`.
4. En el emulador la API se alcanza en `http://10.0.2.2:5080/` (ya viene en `api.base.url`).
   El tráfico HTTP sin TLS solo se permite hacia `10.0.2.2` (`network_security_config.xml`);
   por eso las credenciales viajan cifradas con AES-256-GCM.

Flujo de demostración:

1. **Registrar administrador en este dispositivo** con `admin@sabortico.cr` / `Admin123!`.
   Con `admin.sinmovil@sabortico.cr` la API responde 403 y no se guarda nada.
2. **Ingresar** con las mismas credenciales: aparece el listado de compras y reservas.
3. Filtrar por sede o buscar por nombre; tocar una fila abre el detalle.
4. **Perfil > Cerrar sesión** vuelve al ingreso; «Atrás» no regresa al listado.

## 4. Pruebas y APK

```bash
cd Android
./gradlew testDebugUnitTest      # 13 pruebas unitarias (cifrado, hash, filtro, parser)
./gradlew assembleDebug          # APK de depuración
```

APK de producción: **Build > Generate Signed Bundle / APK** con un almacén de claves propio
(`keystore.*` en `local.properties`). El almacén y sus contraseñas **no** van al repositorio ni al `.zip`.

## 5. Extensiones implementadas

- **E1** Modo sin conexión: el último listado se guarda en SQLite y se muestra marcado como «sin conexión».
- **E8** Pruebas automatizadas: 13 pruebas JUnit.
- **E9** Compilación automática del APK con GitHub Actions en cada `push`.
