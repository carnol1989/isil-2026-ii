package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.controller.dto.ApiErrorResponse;
import com.isil.edu.pe.spedidos.exception.AnulacionPedidoException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce excepciones del flujo PA2 a respuestas HTTP coherentes. */
@RestControllerAdvice(assignableTypes = AnulacionPedidoController.class)
public class ApiExceptionHandler {

  @ExceptionHandler(AnulacionPedidoException.class)
  public ResponseEntity<ApiErrorResponse> handleAnulacion(
      AnulacionPedidoException exception,
      HttpServletRequest request
  ) {
    HttpStatus status = switch (exception.getTipo()) {
      case PEDIDO_NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
      case ANULACION_DUPLICADA, PEDIDO_NO_REGISTRADO -> HttpStatus.CONFLICT;
      case RESPUESTA_INVENTARIO_INVALIDA, ERROR_INVENTARIO -> HttpStatus.BAD_GATEWAY;
    };
    return build(status, exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(InventarioNoDisponibleException.class)
  public ResponseEntity<ApiErrorResponse> handleInventarioNoDisponible(
      InventarioNoDisponibleException exception,
      HttpServletRequest request
  ) {
    return build(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request.getRequestURI());
  }

  private static ResponseEntity<ApiErrorResponse> build(
      HttpStatus status,
      String message,
      String path
  ) {
    ApiErrorResponse body = new ApiErrorResponse(
        status.value(),
        status.getReasonPhrase(),
        message,
        path,
        LocalDateTime.now()
    );
    return ResponseEntity.status(status).body(body);
  }
}
