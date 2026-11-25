package com.isil.edu.pe.pedidos.reportes.service;

import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticaRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionReporte;
import com.isil.edu.pe.pedidos.reportes.service.domain.ProductoVendido;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenPedidos;
import java.util.List;

public interface PedidoReporteService {

  Pedido obtenerPorId(long id);

  List<Pedido> listarTodos();

  ResumenPedidos obtenerResumen();

  List<Pedido> buscarPorCliente(String cliente);

  List<ProductoVendido> listarProductosMasVendidos();

  /** PA4: estado null/vacío = todos; PENDIENTE; REVISADO. Otro valor => EstadoRevisionInvalidoException. */
  List<PedidoRevisionReporte> listarRevisiones(String estado);

  /** PA4: totales y porcentaje de revisión. */
  EstadisticaRevision obtenerEstadisticaRevisiones();

}
