package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Pedido(
    Long id,
    String cliente,
    String productoCodigo,
    String productoNombre,
    BigDecimal precioUnitario,
    int cantidad,
    BigDecimal total,
    LocalDateTime fecha
) { }
