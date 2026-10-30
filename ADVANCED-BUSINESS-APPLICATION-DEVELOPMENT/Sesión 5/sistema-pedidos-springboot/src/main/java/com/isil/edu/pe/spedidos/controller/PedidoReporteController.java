package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.PedidoResponse;
import com.isil.edu.pe.spedidos.controller.dto.ProductoVendidoDto;
import com.isil.edu.pe.spedidos.controller.dto.ResumenPedidosDto;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes/pedidos")
public class PedidoReporteController {

  private final PedidoReporteService
      reporteService;

  public PedidoReporteController(
      PedidoReporteService reporteService) {

    this.reporteService =
        reporteService;
  }

  @GetMapping("/resumen")
  public ResumenPedidosDto obtenerResumen() {
    return ResumenPedidosDto.from(reporteService.obtenerResumen()
    );
  }

  @GetMapping("/buscar")
  public List<PedidoResponse> buscarPorCliente(@RequestParam String cliente) {
    return reporteService.buscarPorCliente(cliente)
        .stream()
        .map(PedidoResponse::from)
        .toList();
  }

  @GetMapping("/productos")
  public List<ProductoVendidoDto> listarProductosMasVendidos() {
    return reporteService
        .listarProductosMasVendidos()
        .stream()
        .map(ProductoVendidoDto::from)
        .toList();
  }
}

