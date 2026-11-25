package com.isil.edu.pe.pedidos.reportes.exception;

public class EstadoRevisionInvalidoException extends RuntimeException {

  public EstadoRevisionInvalidoException(String estado) {
    super("El estado '" + estado + "' no es válido. Valores permitidos: PENDIENTE, REVISADO.");
  }
}
