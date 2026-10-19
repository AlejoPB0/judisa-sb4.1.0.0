-- Datos de demostración (solo perfil dev): 5 empleados, 6 referencias, 30 cortes, 15 pagos.
-- Las fechas son relativas al día en que se aplica la migración para que el panel muestre datos del mes.

INSERT INTO empleados (nombre, documento, telefono, tipo_trabajo) VALUES
    ('María Fernanda Rojas', '1090456789', '3104567890', 'CORTADOR'),
    ('Luis Alberto Contreras', '1093221456', '3157894561', 'CORTADOR'),
    ('Diana Carolina Peñaranda', '1090987123', '3012345678', 'CONFECCIONISTA'),
    ('Jhon Freddy Ortega', '88234567', '3209876543', 'TERMINACION'),
    ('Yesica Paola Durán', '1004567321', '3176543210', 'PLANCHADO');

INSERT INTO referencias (codigo, descripcion, valor_unitario) VALUES
    ('REF-2045', 'Jean dama skinny tiro alto', 1800),
    ('REF-2046', 'Jean dama bota recta', 1700),
    ('REF-3010', 'Jean caballero clásico', 2000),
    ('REF-3015', 'Jean caballero slim fit', 2100),
    ('REF-4001', 'Short dama rasgado', 1200),
    ('REF-5020', 'Chaqueta jean unisex', 3500);

INSERT INTO cortes (referencia_id, empleado_id, cantidad, fecha, observaciones) VALUES
    ((SELECT id FROM referencias WHERE codigo = 'REF-2046'), (SELECT id FROM empleados WHERE documento = '1093221456'), 160, CURRENT_DATE - INTERVAL 6 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1004567321'), 70, CURRENT_DATE - INTERVAL 46 DAY, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090456789'), 100, CURRENT_DATE - INTERVAL 4 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1093221456'), 60, CURRENT_DATE - INTERVAL 30 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1090987123'), 50, CURRENT_DATE - INTERVAL 72 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090456789'), 240, CURRENT_DATE - INTERVAL 74 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090987123'), 160, CURRENT_DATE - INTERVAL 6 DAY, 'Tela con defecto en 3 piezas'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090456789'), 80, CURRENT_DATE - INTERVAL 37 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090456789'), 70, CURRENT_DATE - INTERVAL 73 DAY, 'Urgente para despacho del viernes'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090987123'), 90, CURRENT_DATE - INTERVAL 13 DAY, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090987123'), 100, CURRENT_DATE - INTERVAL 47 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090987123'), 60, CURRENT_DATE - INTERVAL 72 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2046'), (SELECT id FROM empleados WHERE documento = '1090987123'), 190, CURRENT_DATE - INTERVAL 68 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1004567321'), 180, CURRENT_DATE - INTERVAL 74 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1093221456'), 110, CURRENT_DATE - INTERVAL 23 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2046'), (SELECT id FROM empleados WHERE documento = '1004567321'), 60, CURRENT_DATE - INTERVAL 73 DAY, 'Urgente para despacho del viernes'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1090987123'), 140, CURRENT_DATE - INTERVAL 57 DAY, 'Urgente para despacho del viernes'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2045'), (SELECT id FROM empleados WHERE documento = '1090987123'), 70, CURRENT_DATE - INTERVAL 65 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1090456789'), 80, CURRENT_DATE - INTERVAL 62 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090456789'), 60, CURRENT_DATE - INTERVAL 71 DAY, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1004567321'), 140, CURRENT_DATE - INTERVAL 44 DAY, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1093221456'), 180, CURRENT_DATE - INTERVAL 8 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1093221456'), 250, CURRENT_DATE - INTERVAL 8 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '88234567'), 130, CURRENT_DATE - INTERVAL 73 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1004567321'), 130, CURRENT_DATE - INTERVAL 49 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2045'), (SELECT id FROM empleados WHERE documento = '1093221456'), 180, CURRENT_DATE - INTERVAL 45 DAY, 'Tela con defecto en 3 piezas'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2045'), (SELECT id FROM empleados WHERE documento = '1090987123'), 190, CURRENT_DATE - INTERVAL 7 DAY, 'Tela con defecto en 3 piezas'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1004567321'), 80, CURRENT_DATE - INTERVAL 31 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1093221456'), 60, CURRENT_DATE - INTERVAL 21 DAY, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1093221456'), 120, CURRENT_DATE - INTERVAL 17 DAY, NULL);

INSERT INTO pagos (empleado_id, piezas, valor_unitario, total, nota, fecha, estado, fecha_pago_efectivo) VALUES
    ((SELECT id FROM empleados WHERE documento = '1090456789'), 130, 1200, 156000, 'Pago de producción', CURRENT_DATE - INTERVAL 53 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 53 DAY),
    ((SELECT id FROM empleados WHERE documento = '1093221456'), 260, 2000, 520000, 'Pago de producción', CURRENT_DATE - INTERVAL 48 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 48 DAY),
    ((SELECT id FROM empleados WHERE documento = '1090987123'), 90, 1700, 153000, 'Pago de producción', CURRENT_DATE - INTERVAL 10 DAY, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '88234567'), 90, 1700, 153000, 'Pago de producción', CURRENT_DATE - INTERVAL 29 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 29 DAY),
    ((SELECT id FROM empleados WHERE documento = '1004567321'), 120, 3500, 420000, 'Pago de producción', CURRENT_DATE - INTERVAL 1 DAY, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '1090456789'), 230, 2100, 483000, 'Pago de producción', CURRENT_DATE - INTERVAL 23 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 23 DAY),
    ((SELECT id FROM empleados WHERE documento = '1093221456'), 140, 2000, 280000, 'Pago de producción', CURRENT_DATE - INTERVAL 0 DAY, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '1090987123'), 180, 1700, 306000, 'Pago de producción', CURRENT_DATE - INTERVAL 68 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 68 DAY),
    ((SELECT id FROM empleados WHERE documento = '88234567'), 240, 2000, 480000, 'Pago de producción', CURRENT_DATE - INTERVAL 40 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 40 DAY),
    ((SELECT id FROM empleados WHERE documento = '1004567321'), 270, 1700, 459000, 'Pago de producción', CURRENT_DATE - INTERVAL 65 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 65 DAY),
    ((SELECT id FROM empleados WHERE documento = '1090456789'), 250, 1200, 300000, 'Pago de producción', CURRENT_DATE - INTERVAL 6 DAY, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '1093221456'), 290, 2100, 609000, 'Pago de producción', CURRENT_DATE - INTERVAL 50 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 50 DAY),
    ((SELECT id FROM empleados WHERE documento = '1090987123'), 170, 2100, 357000, 'Pago de producción', CURRENT_DATE - INTERVAL 50 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 50 DAY),
    ((SELECT id FROM empleados WHERE documento = '88234567'), 200, 1800, 360000, 'Pago de producción', CURRENT_DATE - INTERVAL 51 DAY, 'REALIZADO', CURRENT_DATE - INTERVAL 51 DAY),
    ((SELECT id FROM empleados WHERE documento = '1004567321'), 110, 1800, 198000, 'Pago de producción', CURRENT_DATE - INTERVAL 8 DAY, 'PENDIENTE', NULL);
