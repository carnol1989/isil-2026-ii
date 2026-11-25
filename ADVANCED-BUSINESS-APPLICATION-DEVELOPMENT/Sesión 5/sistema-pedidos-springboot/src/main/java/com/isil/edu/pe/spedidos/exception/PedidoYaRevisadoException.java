package com.isil.edu.pe.spedidos.exception;

/** El pedido ya fue revisado; no se permite una segunda revisión. */
public class PedidoYaRevisadoException extends PedidoException {

  public PedidoYaRevisadoException(long pedidoId) {
    super("El pedido " + pedidoId + " ya fue revisado. No se permite una segunda revisión.");
  }
}
