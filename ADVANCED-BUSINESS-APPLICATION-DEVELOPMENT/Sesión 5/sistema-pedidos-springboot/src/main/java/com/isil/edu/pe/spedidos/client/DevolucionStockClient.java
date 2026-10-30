package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;

/**
 * Puerto de salida utilizado para devolver stock al sistema remoto de Inventario.
 *
 * <p>La capa de negocio depende de esta abstracción y no de RestClient.</p>
 */
public interface DevolucionStockClient {

  /**
   * Devuelve unidades al producto identificado por código.
   *
   * @param codigo código de negocio del producto
   * @param cantidad unidades que deben reincorporarse
   * @return confirmación remota de la devolución
   */
  DevolucionStockResponse devolverStock(String codigo, int cantidad);
}
