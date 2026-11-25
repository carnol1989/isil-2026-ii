package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.ProductoVendido;
import java.math.BigDecimal;

public record ProductoVendidoDto(
    String productoCodigo,
    String productoNombre,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado
) {
  public static ProductoVendidoDto from(ProductoVendido producto) {
    return new ProductoVendidoDto(
        producto.productoCodigo(),
        producto.productoNombre(),
        producto.totalPedidos(),
        producto.totalUnidades(),
        producto.importeAcumulado()
    );
  }

}

