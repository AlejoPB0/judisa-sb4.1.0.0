-- Judisa · Esquema inicial (MySQL 8, perfil alternativo)
-- Las marcas de tiempo se guardan en UTC (DATETIME(6)); Hibernate convierte con hibernate.jdbc.time_zone.

CREATE TABLE usuarios (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    username               VARCHAR(50)  NOT NULL,
    password_hash          VARCHAR(100) NOT NULL,
    nombre_completo        VARCHAR(120) NOT NULL,
    rol                    VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    activo                 BOOLEAN      NOT NULL DEFAULT TRUE,
    debe_cambiar_password  BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos      INT          NOT NULL DEFAULT 0,
    bloqueado_hasta        DATETIME(6),
    created_at             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('ADMIN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,
    expira_en   DATETIME(6) NOT NULL,
    revocado    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE empleados (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    documento     VARCHAR(20)  NOT NULL,
    telefono      VARCHAR(15)  NOT NULL,
    tipo_trabajo  VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_empleados_documento UNIQUE (documento),
    CONSTRAINT ck_empleados_tipo CHECK (tipo_trabajo IN
        ('CORTADOR', 'CONFECCIONISTA', 'TERMINACION', 'PLANCHADO', 'EMPAQUE', 'OTRO')),
    INDEX idx_empleados_nombre (nombre),
    INDEX idx_empleados_tipo (tipo_trabajo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE referencias (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(30)    NOT NULL,
    descripcion     VARCHAR(200)   NOT NULL,
    valor_unitario  DECIMAL(14, 0) NOT NULL,
    activa          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_referencias_codigo UNIQUE (codigo),
    CONSTRAINT ck_referencias_valor CHECK (valor_unitario > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cortes (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    referencia_id  BIGINT       NOT NULL,
    empleado_id    BIGINT       NOT NULL,
    cantidad       INT          NOT NULL,
    fecha          DATE         NOT NULL,
    observaciones  VARCHAR(500),
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT ck_cortes_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_cortes_referencia FOREIGN KEY (referencia_id) REFERENCES referencias (id),
    CONSTRAINT fk_cortes_empleado FOREIGN KEY (empleado_id) REFERENCES empleados (id),
    INDEX idx_cortes_fecha (fecha)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE pagos (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id          BIGINT         NOT NULL,
    piezas               INT            NOT NULL,
    valor_unitario       DECIMAL(14, 0) NOT NULL,
    total                DECIMAL(16, 0) NOT NULL,
    nota                 VARCHAR(300),
    fecha                DATE           NOT NULL,
    estado               VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago_efectivo  DATE,
    created_at           DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT ck_pagos_piezas CHECK (piezas > 0),
    CONSTRAINT ck_pagos_valor CHECK (valor_unitario > 0),
    CONSTRAINT ck_pagos_estado CHECK (estado IN ('PENDIENTE', 'REALIZADO')),
    CONSTRAINT fk_pagos_empleado FOREIGN KEY (empleado_id) REFERENCES empleados (id),
    INDEX idx_pagos_fecha (fecha),
    INDEX idx_pagos_estado (estado)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
