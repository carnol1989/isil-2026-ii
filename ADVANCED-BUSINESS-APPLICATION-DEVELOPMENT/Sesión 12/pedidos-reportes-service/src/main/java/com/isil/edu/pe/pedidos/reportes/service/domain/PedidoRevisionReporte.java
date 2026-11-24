package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pedido con su estado de revisión (observación y fecha solo si fue revisado). */
public record PedidoRevisionReporte(
    Long id,
    String cliente,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String observacion,
    LocalDateTime fechaRevision
) { }
