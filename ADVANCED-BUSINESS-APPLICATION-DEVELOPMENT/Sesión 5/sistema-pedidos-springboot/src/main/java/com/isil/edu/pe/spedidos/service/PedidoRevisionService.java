package com.isil.edu.pe.spedidos.service;

public interface PedidoRevisionService {

  /**
   * Marca un pedido como revisado y registra su trazabilidad en una sola transacción local.
   *
   * @throws com.isil.edu.pe.spedidos.exception.PedidoException si los datos son inválidos
   * @throws com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException si el pedido no existe
   * @throws com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException si ya fue revisado
   */
  void revisarPedido(long pedidoId, String observacion);
}
