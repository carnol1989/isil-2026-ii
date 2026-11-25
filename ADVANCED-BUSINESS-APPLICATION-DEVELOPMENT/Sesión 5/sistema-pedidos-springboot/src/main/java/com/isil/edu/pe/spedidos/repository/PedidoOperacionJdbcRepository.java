package com.isil.edu.pe.spedidos.repository;

import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JDBC para operaciones relacionadas con pedidos.
 * Proporciona métodos para actualizar el precio unitario y recalcular el total de un pedido.
 */
@Repository
public class PedidoOperacionJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  public PedidoOperacionJdbcRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Actualiza el precio unitario de un pedido específico.
   *
   * @param pedidoId          El ID del pedido a actualizar.
   * @param nuevoPrecioUnitario El nuevo precio unitario a establecer.
   * @return El número de filas afectadas por la actualización.
   */
  public int actualizarPrecioUnitario(long pedidoId, BigDecimal nuevoPrecioUnitario) {
    String sql = "UPDATE pedido SET precio_unitario = ? WHERE id = ?";
    return jdbcTemplate.update(sql, nuevoPrecioUnitario, pedidoId);
  }

  /**
   * Recalcula el total de un pedido específico basado en el precio unitario y la cantidad.
   *
   * @param pedidoId El ID del pedido a recalcular.
   * @return El número de filas afectadas por la actualización.
   */
  public int recalcularTotalPedido(long pedidoId) {
    String sql = "UPDATE pedido SET total = precio_unitario * cantidad WHERE id = ?";
    return jdbcTemplate.update(sql, pedidoId);
  }

}
