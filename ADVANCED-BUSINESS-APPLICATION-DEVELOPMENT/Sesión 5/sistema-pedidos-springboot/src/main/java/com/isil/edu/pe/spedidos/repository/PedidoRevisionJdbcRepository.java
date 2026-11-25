package com.isil.edu.pe.spedidos.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring JDBC del módulo de revisión (PA4).
 * Todo el SQL es parametrizado: ningún valor del usuario se concatena.
 * No abre transacciones: el límite transaccional lo define el Service.
 */
@Repository
public class PedidoRevisionJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  public PedidoRevisionJdbcRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /** Marca el pedido como revisado solo si aún está pendiente. @return filas afectadas (0 o 1). */
  public int marcarComoRevisado(long pedidoId) {
    String sql = "UPDATE pedido SET revisado = true WHERE id = ? AND revisado = false";
    return jdbcTemplate.update(sql, pedidoId);
  }

  /** Inserta la evidencia de trazabilidad. @return filas insertadas. */
  public int insertarRevision(long pedidoId, String observacion, LocalDateTime fechaRevision) {
    String sql = "INSERT INTO pedido_revision (pedido_id, observacion, fecha_revision) VALUES (?, ?, ?)";
    return jdbcTemplate.update(sql, pedidoId, observacion, Timestamp.valueOf(fechaRevision));
  }

  public boolean existePedido(long pedidoId) {
    String sql = "SELECT COUNT(*) FROM pedido WHERE id = ?";
    Integer total = jdbcTemplate.queryForObject(sql, Integer.class, pedidoId);
    return total != null && total > 0;
  }
}
