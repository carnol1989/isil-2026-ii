package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;

public record ResumenPedidosResponse(
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) { }
