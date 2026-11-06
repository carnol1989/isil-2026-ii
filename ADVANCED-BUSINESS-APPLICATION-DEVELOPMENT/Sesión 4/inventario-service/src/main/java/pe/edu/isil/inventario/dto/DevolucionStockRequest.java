package pe.edu.isil.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para solicitar la devolución de unidades al inventario.
 */
public class DevolucionStockRequest {

  @NotNull(message = "La cantidad es obligatoria.")
  @Positive(message = "La cantidad debe ser mayor que cero.")
  private Integer cantidad;

  public DevolucionStockRequest() {
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
