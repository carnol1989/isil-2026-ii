package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import java.util.List;

/** Contrato de negocio para registrar y consultar anulaciones. */
public interface AnulacionPedidoService {

  AnulacionPedido anular(Long pedidoId, String motivo);

  List<AnulacionPedido> listar();
}
