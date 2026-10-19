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
    ((SELECT id FROM referencias WHERE codigo = 'REF-2046'), (SELECT id FROM empleados WHERE documento = '1093221456'), 160, CURRENT_DATE - 6, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1004567321'), 70, CURRENT_DATE - 46, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090456789'), 100, CURRENT_DATE - 4, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1093221456'), 60, CURRENT_DATE - 30, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1090987123'), 50, CURRENT_DATE - 72, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090456789'), 240, CURRENT_DATE - 74, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090987123'), 160, CURRENT_DATE - 6, 'Tela con defecto en 3 piezas'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090456789'), 80, CURRENT_DATE - 37, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1090456789'), 70, CURRENT_DATE - 73, 'Urgente para despacho del viernes'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090987123'), 90, CURRENT_DATE - 13, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090987123'), 100, CURRENT_DATE - 47, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090987123'), 60, CURRENT_DATE - 72, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2046'), (SELECT id FROM empleados WHERE documento = '1090987123'), 190, CURRENT_DATE - 68, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1004567321'), 180, CURRENT_DATE - 74, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1093221456'), 110, CURRENT_DATE - 23, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2046'), (SELECT id FROM empleados WHERE documento = '1004567321'), 60, CURRENT_DATE - 73, 'Urgente para despacho del viernes'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1090987123'), 140, CURRENT_DATE - 57, 'Urgente para despacho del viernes'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2045'), (SELECT id FROM empleados WHERE documento = '1090987123'), 70, CURRENT_DATE - 65, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1090456789'), 80, CURRENT_DATE - 62, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '1090456789'), 60, CURRENT_DATE - 71, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1004567321'), 140, CURRENT_DATE - 44, 'Trazo nuevo'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1093221456'), 180, CURRENT_DATE - 8, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1093221456'), 250, CURRENT_DATE - 8, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-5020'), (SELECT id FROM empleados WHERE documento = '88234567'), 130, CURRENT_DATE - 73, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1004567321'), 130, CURRENT_DATE - 49, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2045'), (SELECT id FROM empleados WHERE documento = '1093221456'), 180, CURRENT_DATE - 45, 'Tela con defecto en 3 piezas'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-2045'), (SELECT id FROM empleados WHERE documento = '1090987123'), 190, CURRENT_DATE - 7, 'Tela con defecto en 3 piezas'),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3010'), (SELECT id FROM empleados WHERE documento = '1004567321'), 80, CURRENT_DATE - 31, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-3015'), (SELECT id FROM empleados WHERE documento = '1093221456'), 60, CURRENT_DATE - 21, NULL),
    ((SELECT id FROM referencias WHERE codigo = 'REF-4001'), (SELECT id FROM empleados WHERE documento = '1093221456'), 120, CURRENT_DATE - 17, NULL);

INSERT INTO pagos (empleado_id, piezas, valor_unitario, total, nota, fecha, estado, fecha_pago_efectivo) VALUES
    ((SELECT id FROM empleados WHERE documento = '1090456789'), 130, 1200, 156000, 'Pago de producción', CURRENT_DATE - 53, 'REALIZADO', CURRENT_DATE - 53),
    ((SELECT id FROM empleados WHERE documento = '1093221456'), 260, 2000, 520000, 'Pago de producción', CURRENT_DATE - 48, 'REALIZADO', CURRENT_DATE - 48),
    ((SELECT id FROM empleados WHERE documento = '1090987123'), 90, 1700, 153000, 'Pago de producción', CURRENT_DATE - 10, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '88234567'), 90, 1700, 153000, 'Pago de producción', CURRENT_DATE - 29, 'REALIZADO', CURRENT_DATE - 29),
    ((SELECT id FROM empleados WHERE documento = '1004567321'), 120, 3500, 420000, 'Pago de producción', CURRENT_DATE - 1, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '1090456789'), 230, 2100, 483000, 'Pago de producción', CURRENT_DATE - 23, 'REALIZADO', CURRENT_DATE - 23),
    ((SELECT id FROM empleados WHERE documento = '1093221456'), 140, 2000, 280000, 'Pago de producción', CURRENT_DATE - 0, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '1090987123'), 180, 1700, 306000, 'Pago de producción', CURRENT_DATE - 68, 'REALIZADO', CURRENT_DATE - 68),
    ((SELECT id FROM empleados WHERE documento = '88234567'), 240, 2000, 480000, 'Pago de producción', CURRENT_DATE - 40, 'REALIZADO', CURRENT_DATE - 40),
    ((SELECT id FROM empleados WHERE documento = '1004567321'), 270, 1700, 459000, 'Pago de producción', CURRENT_DATE - 65, 'REALIZADO', CURRENT_DATE - 65),
    ((SELECT id FROM empleados WHERE documento = '1090456789'), 250, 1200, 300000, 'Pago de producción', CURRENT_DATE - 6, 'PENDIENTE', NULL),
    ((SELECT id FROM empleados WHERE documento = '1093221456'), 290, 2100, 609000, 'Pago de producción', CURRENT_DATE - 50, 'REALIZADO', CURRENT_DATE - 50),
    ((SELECT id FROM empleados WHERE documento = '1090987123'), 170, 2100, 357000, 'Pago de producción', CURRENT_DATE - 50, 'REALIZADO', CURRENT_DATE - 50),
    ((SELECT id FROM empleados WHERE documento = '88234567'), 200, 1800, 360000, 'Pago de producción', CURRENT_DATE - 51, 'REALIZADO', CURRENT_DATE - 51),
    ((SELECT id FROM empleados WHERE documento = '1004567321'), 110, 1800, 198000, 'Pago de producción', CURRENT_DATE - 8, 'PENDIENTE', NULL);
