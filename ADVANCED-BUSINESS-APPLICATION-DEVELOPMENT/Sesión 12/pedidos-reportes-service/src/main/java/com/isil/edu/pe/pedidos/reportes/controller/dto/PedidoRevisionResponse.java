package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionReporte;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionResponse(
    Long id,
    String cliente,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String estado,
    String observacion,
    LocalDateTime fechaRevision
) {
  public static PedidoRevisionResponse from(PedidoRevisionReporte p) {
    return new PedidoRevisionResponse(p.id(), p.cliente(), p.productoNombre(), p.total(),
        p.revisado(), p.revisado() ? "REVISADO" : "PENDIENTE", p.observacion(), p.fechaRevision());
  }
}
