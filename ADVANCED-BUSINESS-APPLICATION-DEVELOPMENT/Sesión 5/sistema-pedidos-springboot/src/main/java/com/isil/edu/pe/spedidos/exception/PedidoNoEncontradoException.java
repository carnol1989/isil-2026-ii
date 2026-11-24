package com.isil.edu.pe.spedidos.exception;

/** El pedido que se desea revisar no existe. */
public class PedidoNoEncontradoException extends PedidoException {

  public PedidoNoEncontradoException(long pedidoId) {
    super("No existe un pedido registrado con ID: " + pedidoId);
  }
}
