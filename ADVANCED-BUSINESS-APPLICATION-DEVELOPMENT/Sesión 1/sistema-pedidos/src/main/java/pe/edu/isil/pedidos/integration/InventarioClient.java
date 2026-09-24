package pe.edu.isil.pedidos.integration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;
import pe.edu.isil.pedidos.integration.dto.ApiError;
import pe.edu.isil.pedidos.integration.dto.ProductoInventarioResponse;
import pe.edu.isil.pedidos.integration.dto.ReservaStockRequest;
import pe.edu.isil.pedidos.integration.dto.ReservaStockResponse;

/**
 * Cliente HTTP hacia inventario-service usando Jakarta REST Client API.
 */
@Stateless
public class InventarioClient {

  private static final String PROPERTY_BASE_URL = "inventario.api.base-url";
  private static final String PROPERTY_CONNECT_TIMEOUT = "inventario.api.connect-timeout-ms";
  private static final String PROPERTY_READ_TIMEOUT = "inventario.api.read-timeout-ms";

  private Client client;
  private WebTarget inventarioTarget;

  @PostConstruct
  public void inicializar() {
    String baseUrl = System.getProperty(PROPERTY_BASE_URL);
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalStateException(
          "No se configuró la propiedad " + PROPERTY_BASE_URL
              + " en config/integraciones.properties."
      );
    }

    long connectTimeout = leerLong(PROPERTY_CONNECT_TIMEOUT, 2000L);
    long readTimeout = leerLong(PROPERTY_READ_TIMEOUT, 3000L);

    client = ClientBuilder.newBuilder()
        .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
        .build();

    inventarioTarget = client.target(baseUrl);
  }

  @PreDestroy
  public void cerrar() {
    if (client != null) {
      client.close();
    }
  }

  /**
   * Obtiene de una sola vez el inventario remoto para evitar una llamada HTTP por producto.
   */
  public List<ProductoInventarioResponse> listarProductos() {
    try (Response response = inventarioTarget
        .path("productos")
        .request(MediaType.APPLICATION_JSON)
        .get()) {

      validarRespuesta(response);
      return response.readEntity(new GenericType<List<ProductoInventarioResponse>>() { });
    } catch (ProcessingException e) {
      throw noDisponible(e);
    }
  }

  /**
   * Reserva stock usando un código de negocio estable, no la PK local de H2.
   */
  public ReservaStockResponse reservarStock(String codigo, int cantidad) {
    ReservaStockRequest request = new ReservaStockRequest(cantidad);

    try (Response response = inventarioTarget
        .path("productos")
        .path("codigo")
        .path(codigo)
        .path("reservas")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(request, MediaType.APPLICATION_JSON))) {

      validarRespuesta(response);
      return response.readEntity(ReservaStockResponse.class);
    } catch (ProcessingException e) {
      throw noDisponible(e);
    }
  }

  private static void validarRespuesta(Response response) {
    if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
      return;
    }

    String mensaje = "inventario-service respondió HTTP " + response.getStatus() + ".";

    if (response.hasEntity()) {
      try {
        ApiError error = response.readEntity(ApiError.class);
        if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
          mensaje = error.getMessage();
        }
      } catch (ProcessingException ignored) {
        // Se conserva el mensaje genérico si la respuesta no puede mapearse a ApiError.
      }
    }

    throw new InventarioClientException(response.getStatus(), mensaje);
  }

  private static InventarioNoDisponibleException noDisponible(ProcessingException cause) {
    return new InventarioNoDisponibleException(
        "El servicio de inventario no se encuentra disponible o excedió el tiempo de espera.",
        cause
    );
  }

  private static long leerLong(String property, long defaultValue) {
    String value = System.getProperty(property);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }

    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
          "La propiedad " + property + " debe contener un número entero.",
          e
      );
    }
  }
}
