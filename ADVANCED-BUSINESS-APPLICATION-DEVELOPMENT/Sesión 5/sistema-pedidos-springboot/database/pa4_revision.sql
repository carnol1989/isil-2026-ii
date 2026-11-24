-- PA4 - Cambios de esquema (MySQL, base pedidosdb)
-- Con spring.jpa.hibernate.ddl-auto=update Hibernate ya los crea al iniciar la app.
-- Usar este script solo si prefiere crearlos manualmente o para evidencia.

ALTER TABLE pedido ADD COLUMN revisado BIT(1) NOT NULL DEFAULT b'0';

CREATE TABLE IF NOT EXISTS pedido_revision (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  pedido_id      BIGINT       NOT NULL,
  observacion    VARCHAR(255) NOT NULL,
  fecha_revision DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_pedido_revision_pedido UNIQUE (pedido_id),
  CONSTRAINT fk_pedido_revision_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id)
);

-- El usuario de pedidos-reportes-service solo lee: darle permiso sobre la nueva tabla.
GRANT SELECT ON pedidosdb.pedido_revision TO 'spedidos_reportes'@'localhost';
GRANT SELECT ON pedidosdb.pedido_revision TO 'spedidos_reportes'@'%';  -- si el usuario fue creado con '%'
-- Verificacion:
-- SELECT id, revisado FROM pedido;
-- SELECT * FROM pedido_revision;
