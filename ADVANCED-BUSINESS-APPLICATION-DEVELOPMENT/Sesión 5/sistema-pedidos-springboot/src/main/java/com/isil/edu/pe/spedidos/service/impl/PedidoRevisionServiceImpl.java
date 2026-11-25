package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revisión de pedidos con transacción local Spring JDBC.
 *
 * <p>Frontera transaccional: UPDATE pedido + INSERT pedido_revision se confirman
 * juntos o se revierten juntos (RuntimeException => rollback). Dentro del método NO se
 * realizan llamadas HTTP a otros servicios.</p>
 */
@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService {

  static final int LONGITUD_MAXIMA_OBSERVACION = 255;

  private final PedidoRevisionJdbcRepository repository;

  public PedidoRevisionServiceImpl(PedidoRevisionJdbcRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void revisarPedido(long pedidoId, String observacion) {
    validar(pedidoId, observacion);
    String observacionLimpia = observacion.trim();

    // 1) Primera escritura
    int filasActualizadas = repository.marcarComoRevisado(pedidoId);
    if (filasActualizadas == 0) {
      // No hubo cambios: se distingue el motivo (aún sin escrituras parciales).
      if (!repository.existePedido(pedidoId)) {
        throw new PedidoNoEncontradoException(pedidoId);
      }
      throw new PedidoYaRevisadoException(pedidoId);
    }

    // 2) Segunda escritura: si falla (RuntimeException), Spring revierte también el UPDATE.
    repository.insertarRevision(pedidoId, observacionLimpia, LocalDateTime.now());
  }

  private void validar(long pedidoId, String observacion) {
    if (pedidoId <= 0) {
      throw new PedidoException("El ID del pedido debe ser mayor que cero.");
    }
    if (observacion == null || observacion.isBlank()) {
      throw new PedidoException("La observación de la revisión es obligatoria.");
    }
    if (observacion.trim().length() > LONGITUD_MAXIMA_OBSERVACION) {
      throw new PedidoException(
          "La observación no puede superar " + LONGITUD_MAXIMA_OBSERVACION + " caracteres.");
    }
  }
}
