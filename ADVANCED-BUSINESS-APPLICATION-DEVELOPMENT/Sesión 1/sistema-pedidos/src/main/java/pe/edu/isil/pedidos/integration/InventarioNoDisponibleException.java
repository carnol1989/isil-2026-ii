package pe.edu.isil.pedidos.integration;

import jakarta.ejb.ApplicationException;

/**
 * Error técnico: el servicio remoto no pudo ser contactado o no respondió a tiempo.
 */
@ApplicationException(rollback = true)
public class InventarioNoDisponibleException extends RuntimeException {

  public InventarioNoDisponibleException(String message) {
    super(message);
  }

  public InventarioNoDisponibleException(String message, Throwable cause) {
    super(message, cause);
  }
}
