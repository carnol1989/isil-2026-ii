package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticaRevision;
import java.math.BigDecimal;

public record EstadisticaRevisionDto(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {
  public static EstadisticaRevisionDto from(EstadisticaRevision e) {
    return new EstadisticaRevisionDto(e.totalPedidos(), e.totalRevisados(),
        e.totalPendientes(), e.porcentajeRevisado());
  }
}
