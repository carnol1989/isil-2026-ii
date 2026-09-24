/*
  Restablece el stock de demostración sin recrear la base de datos.
  Útil para repetir la práctica en clase.
*/
USE InventarioDB;
GO

UPDATE dbo.producto_inventario SET stock = 10 WHERE codigo = 'LAP-001';
UPDATE dbo.producto_inventario SET stock = 20 WHERE codigo = 'MON-001';
UPDATE dbo.producto_inventario SET stock = 30 WHERE codigo = 'TEC-001';
GO

SELECT id, codigo, nombre, stock, version
FROM dbo.producto_inventario
ORDER BY id;
GO
