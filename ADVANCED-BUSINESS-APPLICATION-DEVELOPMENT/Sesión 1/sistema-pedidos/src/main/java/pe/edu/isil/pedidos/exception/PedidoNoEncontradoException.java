package pe.edu.isil.pedidos.exception;

import jakarta.ejb.ApplicationException;

/**
 * Excepción de negocio utilizada cuando el pedido solicitado no existe.
 *
 * <p>Se separa del resto de validaciones para que la capa web pueda devolver un HTTP 404
 * sin trasladar conocimiento HTTP a la capa de negocio.</p>
 */
@ApplicationException(rollback = true)
public class PedidoNoEncontradoException extends PedidoException {

  public PedidoNoEncontradoException(String message) {
    super(message);
  }
}
