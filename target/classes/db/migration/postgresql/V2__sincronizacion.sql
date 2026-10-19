-- Sprint 2 · RF-57: identificador generado en el teléfono para operaciones hechas sin conexión
-- y registro de operaciones ya procesadas (idempotencia de POST /api/v1/sync).

ALTER TABLE empleados   ADD COLUMN client_id VARCHAR(36);
ALTER TABLE referencias ADD COLUMN client_id VARCHAR(36);
ALTER TABLE cortes      ADD COLUMN client_id VARCHAR(36);
ALTER TABLE pagos       ADD COLUMN client_id VARCHAR(36);

ALTER TABLE empleados   ADD CONSTRAINT uk_empleados_client_id UNIQUE (client_id);
ALTER TABLE referencias ADD CONSTRAINT uk_referencias_client_id UNIQUE (client_id);
ALTER TABLE cortes      ADD CONSTRAINT uk_cortes_client_id UNIQUE (client_id);
ALTER TABLE pagos       ADD CONSTRAINT uk_pagos_client_id UNIQUE (client_id);

CREATE TABLE sync_operaciones (
    client_id    VARCHAR(36)  PRIMARY KEY,
    entidad      VARCHAR(20)  NOT NULL,
    operacion    VARCHAR(20)  NOT NULL,
    estado       VARCHAR(20)  NOT NULL,
    entidad_id   BIGINT,
    mensaje      VARCHAR(500),
    procesada_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
