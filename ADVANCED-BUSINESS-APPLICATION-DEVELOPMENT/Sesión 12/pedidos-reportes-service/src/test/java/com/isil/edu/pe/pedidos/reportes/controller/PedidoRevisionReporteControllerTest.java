package com.isil.edu.pe.pedidos.reportes.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.isil.edu.pe.pedidos.reportes.exception.ApiExceptionHandler;
import com.isil.edu.pe.pedidos.reportes.exception.EstadoRevisionInvalidoException;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticaRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionReporte;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PedidoRevisionReporteControllerTest {

  @Mock private PedidoReporteService service;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new PedidoRevisionReporteController(service))
        .setControllerAdvice(new ApiExceptionHandler())
        .build();
  }

  @Test
  @DisplayName("GET /revisiones?estado=REVISADO devuelve 200 y JSON con observacion y fecha")
  void revisionesDevuelveJson() throws Exception {
    // Arrange
    when(service.listarRevisiones("REVISADO")).thenReturn(List.of(new PedidoRevisionReporte(
        7L, "Ana", "Laptop", new BigDecimal("200.00"), true, "OK",
        LocalDateTime.of(2026, 11, 20, 10, 15, 30))));

    // Act + Assert
    mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "REVISADO"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$[0].id").value(7))
        .andExpect(jsonPath("$[0].estado").value("REVISADO"))
        .andExpect(jsonPath("$[0].observacion").value("OK"))
        .andExpect(jsonPath("$[0].fechaRevision").exists());
  }

  @Test
  @DisplayName("GET /revisiones con estado invalido devuelve 400")
  void estadoInvalidoDevuelve400() throws Exception {
    // Arrange
    when(service.listarRevisiones("XYZ")).thenThrow(new EstadoRevisionInvalidoException("XYZ"));

    // Act + Assert
    mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "XYZ"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /estadisticas/revisiones devuelve el DTO con los cuatro campos")
  void estadisticasDevuelveDto() throws Exception {
    // Arrange
    when(service.obtenerEstadisticaRevisiones())
        .thenReturn(new EstadisticaRevision(4, 1, 3, new BigDecimal("25.00")));

    // Act + Assert
    mockMvc.perform(get("/api/v1/pedidos/estadisticas/revisiones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPedidos").value(4))
        .andExpect(jsonPath("$.totalRevisados").value(1))
        .andExpect(jsonPath("$.totalPendientes").value(3))
        .andExpect(jsonPath("$.porcentajeRevisado").value(25.00));
  }
}
