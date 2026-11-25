package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.controller.dto.EstadisticaRevisionDto;
import com.isil.edu.pe.pedidos.reportes.controller.dto.PedidoRevisionResponse;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** PA4 - Recursos REST de revisión de pedidos. Solo delega en el Service. */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoRevisionReporteController {

  private final PedidoReporteService reporteService;

  public PedidoRevisionReporteController(PedidoReporteService reporteService) {
    this.reporteService = reporteService;
  }

  /** GET /api/v1/pedidos/revisiones?estado=PENDIENTE|REVISADO (sin estado: todos). 400 si es inválido. */
  @GetMapping(value = "/revisiones", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<PedidoRevisionResponse>> listarRevisiones(
      @RequestParam(name = "estado", required = false) String estado) {
    List<PedidoRevisionResponse> cuerpo = reporteService.listarRevisiones(estado)
        .stream().map(PedidoRevisionResponse::from).toList();
    return ResponseEntity.ok(cuerpo);
  }

  /** GET /api/v1/pedidos/estadisticas/revisiones */
  @GetMapping(value = "/estadisticas/revisiones", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EstadisticaRevisionDto> estadisticasRevisiones() {
    return ResponseEntity.ok(EstadisticaRevisionDto.from(reporteService.obtenerEstadisticaRevisiones()));
  }
}
