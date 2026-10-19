-- Sprint 2 · RF-57: identificador generado en el teléfono e idempotencia de POST /api/v1/sync.

ALTER TABLE empleados   ADD COLUMN client_id VARCHAR(36), ADD CONSTRAINT uk_empleados_client_id UNIQUE (client_id);
ALTER TABLE referencias ADD COLUMN client_id VARCHAR(36), ADD CONSTRAINT uk_referencias_client_id UNIQUE (client_id);
ALTER TABLE cortes      ADD COLUMN client_id VARCHAR(36), ADD CONSTRAINT uk_cortes_client_id UNIQUE (client_id);
ALTER TABLE pagos       ADD COLUMN client_id VARCHAR(36), ADD CONSTRAINT uk_pagos_client_id UNIQUE (client_id);

CREATE TABLE sync_operaciones (
    client_id    VARCHAR(36)  PRIMARY KEY,
    entidad      VARCHAR(20)  NOT NULL,
    operacion    VARCHAR(20)  NOT NULL,
    estado       VARCHAR(20)  NOT NULL,
    entidad_id   BIGINT,
    mensaje      VARCHAR(500),
    procesada_en DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
