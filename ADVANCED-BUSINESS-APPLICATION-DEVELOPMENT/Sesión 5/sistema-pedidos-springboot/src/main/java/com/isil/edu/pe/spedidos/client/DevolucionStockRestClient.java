package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.ApiError;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Implementación HTTP del contrato {@link DevolucionStockClient}.
 *
 * <p>Reutiliza el RestClient administrado por Spring y la URL base externa
 * definida en {@code application.properties}. No crea infraestructura con
 * {@code new} dentro del Service o Controller.</p>
 */
@Component
public class DevolucionStockRestClient implements DevolucionStockClient {

  private final RestClient restClient;
  private final String baseUrl;

  public DevolucionStockRestClient(
      @Qualifier("inventarioHttpClient") RestClient restClient,
      @Value("${inventario.api.base-url}") String baseUrl
  ) {
    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }

  @Override
  public DevolucionStockResponse devolverStock(String codigo, int cantidad) {
    try {
      return restClient
          .post()
          .uri(baseUrl + "/productos/codigo/{codigo}/devoluciones", codigo)
          .body(new DevolucionStockRequest(cantidad))
          .retrieve()
          .body(DevolucionStockResponse.class);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().is5xxServerError()) {
        throw new InventarioNoDisponibleException(
            "inventario-service presentó un error temporal (HTTP "
                + e.getStatusCode().value() + ").",
            e
        );
      }
      throw convertirError(e);
    } catch (ResourceAccessException e) {
      throw new InventarioNoDisponibleException(
          "El servicio de inventario no se encuentra disponible o excedió el tiempo de espera.",
          e
      );
    }
  }

  private InventarioClientException convertirError(RestClientResponseException e) {
    String mensaje = "inventario-service respondió HTTP " + e.getStatusCode().value() + ".";
    try {
      ApiError error = e.getResponseBodyAs(ApiError.class);
      if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
        mensaje = error.getMessage();
      }
    } catch (RuntimeException ignored) {
      // Se conserva el mensaje genérico si el body remoto no coincide con ApiError.
    }
    return new InventarioClientException(e.getStatusCode().value(), mensaje);
  }
}
