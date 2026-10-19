-- Judisa · Esquema inicial (PostgreSQL 16)
-- RF-53 a RF-56: toda la información se almacena en PostgreSQL.

CREATE TABLE usuarios (
    id                     BIGSERIAL PRIMARY KEY,
    username               VARCHAR(50)  NOT NULL,
    password_hash          VARCHAR(100) NOT NULL,
    nombre_completo        VARCHAR(120) NOT NULL,
    rol                    VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    activo                 BOOLEAN      NOT NULL DEFAULT TRUE,
    debe_cambiar_password  BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos      INTEGER      NOT NULL DEFAULT 0,
    bloqueado_hasta        TIMESTAMP WITH TIME ZONE,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMIN'))
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)  NOT NULL,
    expira_en   TIMESTAMP WITH TIME ZONE NOT NULL,
    revocado    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash)
);
CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens (usuario_id);

-- RF-54
CREATE TABLE empleados (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    documento     VARCHAR(20)  NOT NULL,
    telefono      VARCHAR(15)  NOT NULL,
    tipo_trabajo  VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_empleados_documento UNIQUE (documento),  -- RF-12
    CONSTRAINT ck_empleados_tipo CHECK (tipo_trabajo IN
        ('CORTADOR', 'CONFECCIONISTA', 'TERMINACION', 'PLANCHADO', 'EMPAQUE', 'OTRO'))
);
CREATE INDEX idx_empleados_nombre ON empleados (lower(nombre));
CREATE INDEX idx_empleados_tipo ON empleados (tipo_trabajo);

-- RF-55
CREATE TABLE referencias (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(30)    NOT NULL,
    descripcion     VARCHAR(200)   NOT NULL,
    valor_unitario  NUMERIC(14, 0) NOT NULL,
    activa          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uk_referencias_codigo UNIQUE (codigo),
    CONSTRAINT ck_referencias_valor CHECK (valor_unitario > 0)
);

CREATE TABLE cortes (
    id             BIGSERIAL PRIMARY KEY,
    referencia_id  BIGINT       NOT NULL REFERENCES referencias (id),
    empleado_id    BIGINT       NOT NULL REFERENCES empleados (id),
    cantidad       INTEGER      NOT NULL,
    fecha          DATE         NOT NULL,
    observaciones  VARCHAR(500),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT ck_cortes_cantidad CHECK (cantidad > 0)  -- RF-23
);
CREATE INDEX idx_cortes_fecha ON cortes (fecha);
CREATE INDEX idx_cortes_empleado ON cortes (empleado_id);
CREATE INDEX idx_cortes_referencia ON cortes (referencia_id);

-- RF-56 (la lógica del módulo de pagos se construye en el Sprint 3)
CREATE TABLE pagos (
    id                   BIGSERIAL PRIMARY KEY,
    empleado_id          BIGINT         NOT NULL REFERENCES empleados (id),
    piezas               INTEGER        NOT NULL,
    valor_unitario       NUMERIC(14, 0) NOT NULL,
    total                NUMERIC(16, 0) NOT NULL,
    nota                 VARCHAR(300),
    fecha                DATE           NOT NULL,
    estado               VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago_efectivo  DATE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT ck_pagos_piezas CHECK (piezas > 0),
    CONSTRAINT ck_pagos_valor CHECK (valor_unitario > 0),
    CONSTRAINT ck_pagos_estado CHECK (estado IN ('PENDIENTE', 'REALIZADO'))
);
CREATE INDEX idx_pagos_fecha ON pagos (fecha);
CREATE INDEX idx_pagos_empleado ON pagos (empleado_id);
CREATE INDEX idx_pagos_estado ON pagos (estado);
