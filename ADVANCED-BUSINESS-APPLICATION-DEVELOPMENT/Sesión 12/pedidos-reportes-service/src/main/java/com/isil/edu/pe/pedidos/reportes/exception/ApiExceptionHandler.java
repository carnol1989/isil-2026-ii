package com.isil.edu.pe.pedidos.reportes.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ProblemDetail pedidoNoEncontrado(PedidoNoEncontradoException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Pedido no encontrado");
    return problem;
  }

  @ExceptionHandler(EstadoRevisionInvalidoException.class)
  public ProblemDetail estadoInvalido(EstadoRevisionInvalidoException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problem.setTitle("Parámetro inválido");
    return problem;
  }

  /** Errores de base de datos: respuesta genérica, sin detalles técnicos. */
  @ExceptionHandler(DataAccessException.class)
  public ProblemDetail errorDeDatos(DataAccessException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
        "No fue posible consultar la información en este momento.");
    problem.setTitle("Servicio de datos no disponible");
    return problem;
  }
}
