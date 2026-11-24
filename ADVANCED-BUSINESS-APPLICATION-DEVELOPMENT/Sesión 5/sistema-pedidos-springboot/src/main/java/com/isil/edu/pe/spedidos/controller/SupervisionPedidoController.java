package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * PA4 - Supervisión de pedidos (Spring MVC).
 *
 * <pre>
 * GET  /supervision/pedidos                -> PedidoReporteClient -> pedidos-reportes-service
 * POST /supervision/pedidos/{id}/revisar   -> PedidoRevisionService -> Repository JDBC -> MySQL
 * </pre>
 *
 * <p>El controller NO ejecuta SQL: recibe parámetros, delega y construye el Model.
 * El POST aplica Post/Redirect/Get.</p>
 */
@Controller
@RequestMapping("/supervision/pedidos")
public class SupervisionPedidoController {

  private static final Logger log = LoggerFactory.getLogger(SupervisionPedidoController.class);
  static final String VISTA = "supervision-pedidos";
  static final String MSG_REPORTES_NO_DISPONIBLE =
      "El servicio de reportes no está disponible por el momento. Intente nuevamente en unos minutos.";

  private final PedidoRevisionService pedidoRevisionService;
  private final PedidoReporteClient pedidoReporteClient;

  public SupervisionPedidoController(PedidoRevisionService pedidoRevisionService,
      PedidoReporteClient pedidoReporteClient) {
    this.pedidoRevisionService = pedidoRevisionService;
    this.pedidoReporteClient = pedidoReporteClient;
  }

  @GetMapping
  public String supervisar(
      @RequestParam(name = "estado", defaultValue = "") String estado,
      Model model,
      HttpServletResponse response) {
    model.addAttribute("estado", estado);
    try {
      model.addAttribute("pedidos", pedidoReporteClient.listarRevisiones(estado));
      model.addAttribute("estadisticas", pedidoReporteClient.obtenerEstadisticasRevisiones());
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      model.addAttribute("error", "El filtro de estado debe ser PENDIENTE o REVISADO.");
      model.addAttribute("pedidos", List.of());
    } catch (PedidoReporteNoDisponibleException e) {
      log.warn("pedidos-reportes-service no disponible: {}", e.getMessage());
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
      model.addAttribute("error", MSG_REPORTES_NO_DISPONIBLE);
      model.addAttribute("pedidos", List.of());
    }
    return VISTA;
  }

  @PostMapping("/{id}/revisar")
  public String revisar(
      @PathVariable("id") long id,
      @RequestParam(name = "observacion", defaultValue = "") String observacion,
      RedirectAttributes redirectAttributes) {
    try {
      pedidoRevisionService.revisarPedido(id, observacion);
      redirectAttributes.addFlashAttribute("mensaje", "El pedido " + id + " fue revisado correctamente.");
    } catch (PedidoException e) {
      // Validaciones, pedido inexistente o ya revisado: mensaje comprensible, sin cambios.
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    } catch (DataAccessException e) {
      log.error("Error de base de datos al revisar el pedido {}", id, e);
      redirectAttributes.addFlashAttribute("error",
          "No fue posible registrar la revisión. No se aplicó ningún cambio.");
    }
    return "redirect:/supervision/pedidos";   // PRG
  }
}
