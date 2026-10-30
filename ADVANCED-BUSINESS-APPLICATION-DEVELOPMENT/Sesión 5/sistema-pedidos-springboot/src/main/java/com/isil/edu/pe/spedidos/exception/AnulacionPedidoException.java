package com.isil.edu.pe.spedidos.exception;

/** Excepción de negocio del flujo de anulación de pedidos. */
public class AnulacionPedidoException extends RuntimeException {

  public enum Tipo {
    PEDIDO_NO_ENCONTRADO,
    ANULACION_DUPLICADA,
    PEDIDO_NO_REGISTRADO,
    RESPUESTA_INVENTARIO_INVALIDA,
    ERROR_INVENTARIO
  }

  private final Tipo tipo;

  public AnulacionPedidoException(Tipo tipo, String message) {
    super(message);
    this.tipo = tipo;
  }

  public AnulacionPedidoException(Tipo tipo, String message, Throwable cause) {
    super(message, cause);
    this.tipo = tipo;
  }

  public Tipo getTipo() {
    return tipo;
  }
}
