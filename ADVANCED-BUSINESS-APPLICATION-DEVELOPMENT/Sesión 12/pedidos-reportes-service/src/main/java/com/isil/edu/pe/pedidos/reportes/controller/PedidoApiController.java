package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.controller.dto.PedidoResponse;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class PedidoApiController {

  private final PedidoReporteService pedidoReporteService;

  /**
   * Construye el controlador utilizando el servicio proporcionado
   * por el contenedor IoC de Spring.
   *
   * @param pedidoReporteService servicio de negocio de pedidos
   */
  public PedidoApiController(PedidoReporteService pedidoReporteService) {
    this.pedidoReporteService = pedidoReporteService;
  }

  @GetMapping
  public List<PedidoResponse> listarPedidos() {
    return pedidoReporteService.listarTodos()
        .stream()
        .map(PedidoResponse::from)
        .toList();
  }

  @GetMapping(value = "/resumen", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> resumenPedidos() {
    List<Pedido> pedidos = pedidoReporteService.listarTodos();

    int totalUnidades = pedidos.stream()
        .mapToInt(Pedido::cantidad)
        .sum();

    Map<String, Object> resumen = new HashMap<>();

    resumen.put("modulo", "Sistema de Pedidos");
    resumen.put("totalPedidos", pedidos.size());
    resumen.put("totalUnidades", totalUnidades);

    return resumen;
  }
}
