package com.isil.edu.pe.pedidos.reportes.service.impl;

import com.isil.edu.pe.pedidos.reportes.exception.EstadoRevisionInvalidoException;
import com.isil.edu.pe.pedidos.reportes.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.pedidos.reportes.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ConteoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticaRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionReporte;
import com.isil.edu.pe.pedidos.reportes.service.domain.ProductoVendido;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenPedidos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de reporte de pedidos.
 */
@Service
public class PedidoReporteServiceImpl implements PedidoReporteService {

  private final PedidoReporteJdbcRepository jdbcRepository;

  public PedidoReporteServiceImpl(PedidoReporteJdbcRepository jdbcRepository) {
      this.jdbcRepository = jdbcRepository;
  }

  @Override
  public Pedido obtenerPorId(long id) {
    return jdbcRepository.buscarPorId(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
  }

  @Override
  public List<Pedido> listarTodos() {
    return jdbcRepository.listarTodos();
  }

  @Override
  public ResumenPedidos obtenerResumen() {
    ResumenPedidosProjection resumenPedidos = jdbcRepository.obtenerResumen();
    return new ResumenPedidos(resumenPedidos.totalPedidos(), resumenPedidos.totalUnidades(),
        resumenPedidos.importeAcumulado(), resumenPedidos.ticketPromedio());
  }

  @Override
  public List<Pedido> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }

    return jdbcRepository.buscarPorCliente(cliente);
  }

  @Override
  public List<ProductoVendido> listarProductosMasVendidos() {
    return jdbcRepository.listarProductosMasVendidos()
        .stream()
        .map(item ->
            new ProductoVendido(
                item.productoCodigo(),
                item.productoNombre(),
                item.totalPedidos(),
                item.totalUnidades(),
                item.importeAcumulado()
            )
        )
        .toList();
  }

  @Override
  public List<PedidoRevisionReporte> listarRevisiones(String estado) {
    Boolean revisado;
    if (estado == null || estado.isBlank()) {
      revisado = null;
    } else if ("REVISADO".equalsIgnoreCase(estado.trim())) {
      revisado = true;
    } else if ("PENDIENTE".equalsIgnoreCase(estado.trim())) {
      revisado = false;
    } else {
      throw new EstadoRevisionInvalidoException(estado);
    }
    return jdbcRepository.listarRevisiones(revisado);
  }

  @Override
  public EstadisticaRevision obtenerEstadisticaRevisiones() {
    ConteoRevisionProjection conteo = jdbcRepository.contarRevisiones();
    long total = conteo.totalPedidos();
    long revisados = conteo.totalRevisados();
    long pendientes = total - revisados;
    BigDecimal porcentaje = total == 0
        ? BigDecimal.ZERO.setScale(2)
        : BigDecimal.valueOf(revisados).multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    return new EstadisticaRevision(total, revisados, pendientes, porcentaje);
  }
}
