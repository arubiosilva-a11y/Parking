# ProyectoParking

API REST de ParkControl. Requiere Java 25, Maven y PostgreSQL con las tablas del proyecto.

## Arranque con seguridad

1. Configura `DB_URL` (URL JDBC), `DB_USER`, `DB_PASSWORD` y `DB_SCHEMA` en tu entorno o en la configuración de ejecución de IntelliJ.
2. Aplica una vez la migración de seguridad en el mismo esquema de las tablas de ParkControl. Por ejemplo, con una URL PostgreSQL sin el prefijo `jdbc:` y la contraseña gestionada por el cliente `psql`:

   ```bash
   PGOPTIONS="-c search_path=${DB_SCHEMA}" psql "$PARKING_PG_DSN" -v ON_ERROR_STOP=1 -f sql/migration/001_seguridad_parking.sql
   ```

   En IntelliJ o DataGrip, selecciona el esquema `parking` en la consola SQL de la base `postgres` y ejecuta el archivo completo. Si un intento anterior dejó la consola con el mensaje `transacción abortada`, ejecuta `ROLLBACK;` en esa consola antes de reintentar.

   La migración convierte las cuentas existentes con rol `CLIENTE` y abonado asociado en cuentas `ABONADO`. Conserva su vínculo y su contraseña almacenada. Las cuentas de personal pueden quedar sin abono. Si hay correos o vínculos duplicados, datos obligatorios ausentes o roles incompatibles, PostgreSQL revierte la migración.
3. Genera un secreto aleatorio para los JWT y arranca la aplicación:

   ```bash
   export JWT_SECRETO="$(openssl rand -hex 32)"
   ./mvnw spring-boot:run
   ```

   Conserva el mismo secreto fuera del repositorio entre arranques si necesitas que sigan funcionando los tokens ya emitidos. `CORS_ORIGENES` admite una lista de orígenes separados por comas y usa `http://localhost:4200` por defecto. En un entorno compartido, sirve la API por HTTPS.

## Primer administrador y operadores

Un responsable de la base de datos crea las cuentas de personal. Genera un hash BCrypt de la contraseña fuera del repositorio. Por ejemplo, si tienes el paquete Python `bcrypt` instalado, este comando pide la contraseña sin mostrarla:

```bash
python3 -c 'import bcrypt, getpass; print(bcrypt.hashpw(getpass.getpass("Contraseña: ").encode(), bcrypt.gensalt(rounds=10)).decode())'
```

En una sesión interactiva de `psql` con el esquema de ParkControl, introduce el hash y crea la cuenta. Para dar de alta un operador, elige el rol `OPERADOR`.

```sql
\prompt 'Hash BCrypt: ' hash_bcrypt
INSERT INTO usuario (nombre, email, "contraseña", rol)
VALUES ('Administrador', 'admin@example.com', :'hash_bcrypt', 'ADMIN');
\unset hash_bcrypt
```

Si ya existe la cuenta con una contraseña en texto plano, sustituye ese valor por un hash BCrypt generado a partir de una nueva contraseña. Las cuentas de personal con un hash BCrypt compatible y los roles de ParkControl pueden seguir iniciando sesión.

## Acceso a la API

Abre [Swagger UI](http://localhost:8084/swagger-ui.html). Envía `email` y `password` a `POST /api/v1/auth/login`. Copia el `token` en el diálogo **Authorize** de Swagger o envíalo en la cabecera `Authorization: Bearer <token>`. Caduca en 60 minutos.

Para activar la cuenta de un abonado:

1. Un administrador llama a `POST /api/v1/abonados/{id}/activacion` y entrega el `codigo` devuelto al abonado. Caduca a las 24 horas. Si se genera otro código, el anterior queda invalidado.
2. El abonado envía su `email`, el `codigo`, una `password` de al menos 12 caracteres y su `confirmPassword` a `POST /api/v1/auth/registro`. La contraseña admite hasta 72 bytes UTF-8. El correo debe coincidir con el de su ficha.
3. El abonado inicia sesión y ya puede consultar sus tickets. El código queda consumido al crear la cuenta.
