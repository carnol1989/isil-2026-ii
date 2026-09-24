package pe.edu.isil.pedidos.integration;

import jakarta.ejb.ApplicationException;

/**
 * Error devuelto por inventario-service mediante una respuesta HTTP controlada.
 */
@ApplicationException(rollback = true)
public class InventarioClientException extends RuntimeException {

  private final int status;

  public InventarioClientException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
