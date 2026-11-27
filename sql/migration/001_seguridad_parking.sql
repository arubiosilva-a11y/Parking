BEGIN;

UPDATE usuario
SET email = lower(btrim(email)), rol = upper(btrim(rol))
WHERE email IS DISTINCT FROM lower(btrim(email))
   OR rol IS DISTINCT FROM upper(btrim(rol));

UPDATE usuario
SET rol = 'ABONADO'
WHERE rol = 'CLIENTE' AND id_abonado IS NOT NULL;

ALTER TABLE usuario
    ALTER COLUMN nombre SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN "contraseña" SET NOT NULL,
    ALTER COLUMN rol SET NOT NULL,
    ALTER COLUMN id_abonado DROP NOT NULL,
    ADD CONSTRAINT usuario_rol_valido CHECK (rol IN ('ADMIN', 'OPERADOR', 'ABONADO')),
    ADD CONSTRAINT usuario_abonado_requerido CHECK (rol <> 'ABONADO' OR id_abonado IS NOT NULL),
    ADD CONSTRAINT usuario_abonado_unico UNIQUE (id_abonado);

CREATE UNIQUE INDEX usuario_email_ci_unico ON usuario (lower(email));

CREATE TABLE activacion_abonado (
    id_abonado integer PRIMARY KEY REFERENCES abonado(id_abonado) ON DELETE CASCADE,
    codigo_hash varchar(64) NOT NULL UNIQUE,
    expira_en timestamp with time zone NOT NULL
);

COMMIT;
