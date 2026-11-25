package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;

public record ProductoVendidoResponse(
    String productoCodigo,
    String productoNombre,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado
) { }
