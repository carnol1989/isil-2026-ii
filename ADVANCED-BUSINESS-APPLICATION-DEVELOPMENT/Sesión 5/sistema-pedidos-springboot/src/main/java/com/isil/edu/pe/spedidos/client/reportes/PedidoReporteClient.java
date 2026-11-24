package com.isil.edu.pe.spedidos.client.reportes;

import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticaRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ProductoVendidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import java.util.List;

public interface PedidoReporteClient {

  ResumenPedidosResponse obtenerResumen();

  List<PedidoReporteResponse> buscarPorCliente(String cliente);

  List<ProductoVendidoResponse> listarProductosMasVendidos();

  /** PA4: pedidos con su estado de revisión. {@code estado}: PENDIENTE, REVISADO o null/vacío (todos). */
  List<PedidoRevisionReporteResponse> listarRevisiones(String estado);

  /** PA4: resumen estadístico de revisiones. */
  EstadisticaRevisionResponse obtenerEstadisticasRevisiones();

}
