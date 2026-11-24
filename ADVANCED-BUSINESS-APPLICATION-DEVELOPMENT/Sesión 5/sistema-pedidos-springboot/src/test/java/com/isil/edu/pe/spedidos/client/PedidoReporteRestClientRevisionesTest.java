package com.isil.edu.pe.spedidos.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteRestClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticaRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PedidoReporteRestClientRevisionesTest {

  private static final String BASE = "http://localhost:8083/pedidos-reportes-service/api/v1/pedidos";

  @Test
  @DisplayName("Mapea el JSON de revisiones a DTO de transporte")
  void mapeaJsonDeRevisiones() {
    // Arrange
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PedidoReporteRestClient client = new PedidoReporteRestClient(builder.build(), BASE);
    String json = "[{\"id\":7,\"cliente\":\"Ana\",\"productoNombre\":\"Laptop\",\"total\":200.00,"
        + "\"revisado\":true,\"estado\":\"REVISADO\",\"observacion\":\"OK\","
        + "\"fechaRevision\":\"2026-11-20T10:15:30\"}]";
    server.expect(requestTo(BASE + "/revisiones?estado=REVISADO"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    // Act
    List<PedidoRevisionReporteResponse> resultado = client.listarRevisiones("revisado");

    // Assert
    assertEquals(1, resultado.size());
    assertEquals("REVISADO", resultado.get(0).estado());
    assertEquals("OK", resultado.get(0).observacion());
    server.verify();
  }

  @Test
  @DisplayName("Error 5xx del servicio remoto se traduce a PedidoReporteNoDisponibleException")
  void error5xxSeTraduceAExcepcionControlada() {
    // Arrange
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PedidoReporteRestClient client = new PedidoReporteRestClient(builder.build(), BASE);
    server.expect(requestTo(BASE + "/estadisticas/revisiones")).andRespond(withServerError());

    // Act
    PedidoReporteNoDisponibleException ex = assertThrows(PedidoReporteNoDisponibleException.class,
        client::obtenerEstadisticasRevisiones);

    // Assert
    assertTrue(ex.getMessage().contains("no está disponible"));
  }

  @Test
  @DisplayName("Servicio caido (conexion rechazada) se traduce a PedidoReporteNoDisponibleException")
  void conexionRechazadaSeTraduceAExcepcionControlada() {
    // Arrange: puerto 1 cerrado
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofMillis(500));
    factory.setReadTimeout(Duration.ofMillis(500));
    PedidoReporteRestClient client = new PedidoReporteRestClient(
        RestClient.builder().requestFactory(factory).build(), "http://localhost:1/api/v1/pedidos");

    // Act + Assert
    assertThrows(PedidoReporteNoDisponibleException.class, () -> client.listarRevisiones(""));
    assertThrows(PedidoReporteNoDisponibleException.class, client::obtenerEstadisticasRevisiones);
  }

  @Test
  @DisplayName("Estado invalido se rechaza antes de llamar al servicio remoto")
  void estadoInvalidoNoLlamaAlServicio() {
    // Arrange
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PedidoReporteRestClient client = new PedidoReporteRestClient(builder.build(), BASE);

    // Act + Assert
    assertThrows(IllegalArgumentException.class, () -> client.listarRevisiones("XYZ"));
    server.verify(); // sin expectativas: no hubo request
  }

  @Test
  @DisplayName("Mapea el JSON de estadisticas")
  void mapeaEstadisticas() {
    // Arrange
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PedidoReporteRestClient client = new PedidoReporteRestClient(builder.build(), BASE);
    server.expect(requestTo(BASE + "/estadisticas/revisiones")).andRespond(withSuccess(
        "{\"totalPedidos\":4,\"totalRevisados\":1,\"totalPendientes\":3,\"porcentajeRevisado\":25.00}",
        MediaType.APPLICATION_JSON));

    // Act
    EstadisticaRevisionResponse r = client.obtenerEstadisticasRevisiones();

    // Assert
    assertEquals(4, r.totalPedidos());
    assertEquals(3, r.totalPendientes());
    assertEquals(0, new java.math.BigDecimal("25.00").compareTo(r.porcentajeRevisado()));
  }
}
