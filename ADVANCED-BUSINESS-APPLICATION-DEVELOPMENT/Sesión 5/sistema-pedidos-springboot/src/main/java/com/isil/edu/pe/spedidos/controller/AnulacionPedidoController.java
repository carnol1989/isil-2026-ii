package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** API REST del flujo nuevo de anulaciones de PA2. */
@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

  private final AnulacionPedidoService anulacionPedidoService;

  public AnulacionPedidoController(AnulacionPedidoService anulacionPedidoService) {
    this.anulacionPedidoService = anulacionPedidoService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AnulacionPedidoResponse anular(@RequestBody AnulacionPedidoRequest request) {
    AnulacionPedido anulacion = anulacionPedidoService.anular(
        request == null ? null : request.pedidoId(),
        request == null ? null : request.motivo()
    );
    return AnulacionPedidoResponse.from(anulacion);
  }

  @GetMapping
  public List<AnulacionPedidoResponse> listar() {
    return anulacionPedidoService.listar()
        .stream()
        .map(AnulacionPedidoResponse::from)
        .toList();
  }
}
