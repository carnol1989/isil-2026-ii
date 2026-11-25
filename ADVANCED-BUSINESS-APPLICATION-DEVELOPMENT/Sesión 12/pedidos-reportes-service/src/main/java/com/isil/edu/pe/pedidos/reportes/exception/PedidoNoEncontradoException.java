package com.isil.edu.pe.pedidos.reportes.exception;

public class PedidoNoEncontradoException extends RuntimeException {

  public PedidoNoEncontradoException(long id) {
    super("No existe el pedido con id " + id);
  }
}
