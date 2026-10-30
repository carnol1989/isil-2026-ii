package com.isil.edu.pe.spedidos.client.dto;

/** DTO recibido desde inventario-service después de una devolución. */
public record DevolucionStockResponse(
    String codigo,
    int cantidadDevuelta,
    int stockRestante
) {
}
