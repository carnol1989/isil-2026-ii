package com.isil.edu.pe.spedidos.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticaRevisionResponse;
import com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@ExtendWith(MockitoExtension.class)
class SupervisionPedidoControllerTest {

  @Mock private PedidoRevisionService revisionService;
  @Mock private PedidoReporteClient reporteClient;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new SupervisionPedidoController(revisionService, reporteClient))
        .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/", ".jsp"))
        .build();
  }

  @Test
  @DisplayName("GET arma el Model con pedidos y estadisticas del servicio de reportes")
  void getConstruyeModelo() throws Exception {
    // Arrange
    when(reporteClient.listarRevisiones("")).thenReturn(List.of());
    when(reporteClient.obtenerEstadisticasRevisiones())
        .thenReturn(new EstadisticaRevisionResponse(2, 1, 1, new BigDecimal("50.00")));

    // Act + Assert
    mockMvc.perform(get("/supervision/pedidos"))
        .andExpect(status().isOk())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attributeExists("pedidos", "estadisticas"));
  }

  @Test
  @DisplayName("GET con reportes caido responde 503 con mensaje controlado (sin traza)")
  void getConReportesCaidoResponde503() throws Exception {
    // Arrange
    when(reporteClient.listarRevisiones(anyString()))
        .thenThrow(new PedidoReporteNoDisponibleException("conexion rechazada", null));

    // Act + Assert
    mockMvc.perform(get("/supervision/pedidos"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attribute("error", SupervisionPedidoController.MSG_REPORTES_NO_DISPONIBLE));
  }

  @Test
  @DisplayName("POST revisar delega al Service y aplica PRG con mensaje de exito")
  void postRevisarAplicaPrg() throws Exception {
    // Arrange
    doNothing().when(revisionService).revisarPedido(5L, "Todo conforme");

    // Act + Assert
    mockMvc.perform(post("/supervision/pedidos/5/revisar").param("observacion", "Todo conforme"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supervision/pedidos"))
        .andExpect(flash().attributeExists("mensaje"));
    verify(revisionService).revisarPedido(5L, "Todo conforme");
  }

  @Test
  @DisplayName("POST de pedido ya revisado redirige con mensaje de error comprensible")
  void postDuplicadoRedirigeConError() throws Exception {
    // Arrange
    doThrow(new PedidoYaRevisadoException(5L)).when(revisionService).revisarPedido(5L, "Otra");

    // Act + Assert
    mockMvc.perform(post("/supervision/pedidos/5/revisar").param("observacion", "Otra"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supervision/pedidos"))
        .andExpect(flash().attributeExists("error"));
  }
}
