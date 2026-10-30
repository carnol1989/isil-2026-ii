package com.isil.edu.pe.spedidos.controller.dto;

/** Solicitud HTTP para anular un pedido. */
public record AnulacionPedidoRequest(Long pedidoId, String motivo) {
}
