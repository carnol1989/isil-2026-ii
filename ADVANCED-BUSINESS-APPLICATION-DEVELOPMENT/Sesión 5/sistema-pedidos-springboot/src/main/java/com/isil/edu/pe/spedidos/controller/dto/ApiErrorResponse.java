package com.isil.edu.pe.spedidos.controller.dto;

import java.time.LocalDateTime;

/** Formato uniforme para errores de la API local. */
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp
) {
}
