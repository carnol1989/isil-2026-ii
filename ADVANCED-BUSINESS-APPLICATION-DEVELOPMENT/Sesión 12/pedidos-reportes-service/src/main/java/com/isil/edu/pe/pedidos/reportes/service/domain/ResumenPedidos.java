package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;

public record ResumenPedidos(
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}
