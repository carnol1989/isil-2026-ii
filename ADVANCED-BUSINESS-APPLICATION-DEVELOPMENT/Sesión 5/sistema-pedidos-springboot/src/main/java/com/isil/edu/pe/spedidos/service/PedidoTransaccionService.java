package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoOperacionJdbcRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Servicio para manejar transacciones relacionadas con pedidos.
 * Proporciona métodos para actualizar el precio unitario y recalcular el total de un
 * pedido dentro de una transacción.
 */
@Service
public class PedidoTransaccionService {

  private final PedidoOperacionJdbcRepository pedidoOperacionJdbcRepository;
  private final TransactionTemplate transactionTemplate;

  public PedidoTransaccionService(PedidoOperacionJdbcRepository pedidoOperacionJdbcRepository,
    TransactionTemplate transactionTemplate) {
    this.pedidoOperacionJdbcRepository = pedidoOperacionJdbcRepository;
    this.transactionTemplate = transactionTemplate;
  }

  /**
   * Actualiza el precio unitario de un pedido y recalcula su total dentro de una transacción.
   *
   * @param pedidoId          El ID del pedido a actualizar.
   * @param nuevoPrecioUnitario El nuevo precio unitario a establecer.
   * @throws PedidoException Si ocurre algún error durante la actualización o recalculación.
   */
  @Transactional
  public void actualizarPrecioUnitarioYrecalcularTotal(long pedidoId, BigDecimal nuevoPrecioUnitario) {
    validarEntrada(pedidoId, nuevoPrecioUnitario);

    int filasAfectadasPrecioUnitario = pedidoOperacionJdbcRepository.actualizarPrecioUnitario(pedidoId, nuevoPrecioUnitario);
    if (filasAfectadasPrecioUnitario != 1) {
      throw new PedidoException("No se encontró el pedido con ID: " + pedidoId);
    }

    int filasAfectadasTotal = pedidoOperacionJdbcRepository.recalcularTotalPedido(pedidoId);
    if (filasAfectadasTotal != 1) {
      throw new PedidoException("Error fue posible recalcular el total del pedido con ID: " + pedidoId);
    }
  }

  /**
   * Actualiza el precio unitario de un pedido y recalcula su total dentro de una transacción programática.
   *
   * @param pedidoId          El ID del pedido a actualizar.
   * @param nuevoPrecioUnitario El nuevo precio unitario a establecer.
   * @return true si la operación fue exitosa, false en caso contrario.
   */
  public boolean actualizarPrecioUnitarioYrecalcularTotalProgramatico(long pedidoId, BigDecimal nuevoPrecioUnitario) {
    validarEntrada(pedidoId, nuevoPrecioUnitario);

    Boolean resultado = transactionTemplate.execute(status -> {
      int filasAfectadasPrecioUnitario = pedidoOperacionJdbcRepository.actualizarPrecioUnitario(pedidoId, nuevoPrecioUnitario);
      if (filasAfectadasPrecioUnitario != 1) {
        status.setRollbackOnly();
        return false;
      }

      int filasAfectadasTotal = pedidoOperacionJdbcRepository.recalcularTotalPedido(pedidoId);
      if (filasAfectadasTotal != 1) {
        status.setRollbackOnly();
        return false;
      }

      return true;
    });

    return Boolean.TRUE.equals(resultado);
  }

  /**
   * Valida los parámetros de entrada para la actualización del precio unitario.
   *
   * @param pedidoId          El ID del pedido a actualizar.
   * @param nuevoPrecioUnitario El nuevo precio unitario a establecer.
   * @throws PedidoException Si el ID del pedido es menor o igual a cero o si el precio unitario es nulo o negativo.
   */
  private void validarEntrada(long pedidoId, BigDecimal nuevoPrecioUnitario) {
    if (pedidoId <= 0) {
      throw new PedidoException("El ID del pedido debe ser mayor que cero.");
    }
    if (nuevoPrecioUnitario == null || nuevoPrecioUnitario.signum() <= 0) {
      throw new PedidoException("El precio unitario no puede ser negativo.");
    }
  }

}
