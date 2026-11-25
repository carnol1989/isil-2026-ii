package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador web para la visualización de reportes de pedidos.
 */
@Controller
@RequestMapping("/reportes/pedidos")
public class PedidoReporteWebController {

  private final PedidoReporteClient pedidoReporteClient;

    public PedidoReporteWebController(PedidoReporteClient pedidoReporteClient) {
        this.pedidoReporteClient = pedidoReporteClient;
    }

    @GetMapping
    public String verReporteController(
        @RequestParam(name = "cliente", defaultValue = "") String cliente,
        Model model,
        HttpServletResponse response) {
      try {
        model.addAttribute("productos", pedidoReporteClient.listarProductosMasVendidos());
        model.addAttribute("pedidos", pedidoReporteClient.buscarPorCliente(cliente));
        model.addAttribute("resumen", pedidoReporteClient.obtenerResumen());
        model.addAttribute("cliente", cliente);
        return "reportes-pedidos";
      } catch (PedidoReporteNoDisponibleException e) {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        model.addAttribute("error", e.getMessage());
        return "error";
      }
    }

}
