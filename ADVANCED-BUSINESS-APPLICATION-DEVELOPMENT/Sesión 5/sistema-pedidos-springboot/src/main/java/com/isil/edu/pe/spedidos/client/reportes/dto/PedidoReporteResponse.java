package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoReporteResponse(
    Long id,
    String cliente,
    String productoCodigo,
    String productoNombre,
    BigDecimal precioUnitario,
    int cantidad,
    BigDecimal total,
    LocalDateTime fecha
) { }
