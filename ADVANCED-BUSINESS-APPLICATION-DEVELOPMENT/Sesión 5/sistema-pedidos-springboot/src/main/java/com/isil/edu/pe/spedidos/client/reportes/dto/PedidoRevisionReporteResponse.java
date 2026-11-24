package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO de transporte (no es entidad JPA) del recurso GET /api/v1/pedidos/revisiones. */
public record PedidoRevisionReporteResponse(
    Long id,
    String cliente,
    String productoNombre,
    BigDecimal total,
    boolean revisado,
    String estado,
    String observacion,
    LocalDateTime fechaRevision
) { }
