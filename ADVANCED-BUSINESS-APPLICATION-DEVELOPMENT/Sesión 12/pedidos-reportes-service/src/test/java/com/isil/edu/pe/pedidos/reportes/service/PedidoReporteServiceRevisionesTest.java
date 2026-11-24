package com.isil.edu.pe.pedidos.reportes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.pedidos.reportes.exception.EstadoRevisionInvalidoException;
import com.isil.edu.pe.pedidos.reportes.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ConteoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticaRevision;
import com.isil.edu.pe.pedidos.reportes.service.impl.PedidoReporteServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoReporteServiceRevisionesTest {

  @Mock private PedidoReporteJdbcRepository repository;

  private PedidoReporteServiceImpl service() {
    return new PedidoReporteServiceImpl(repository);
  }

  @Test
  @DisplayName("Estadisticas: calcula pendientes y porcentaje redondeado a 2 decimales")
  void calculaEstadisticas() {
    // Arrange
    when(repository.contarRevisiones()).thenReturn(new ConteoRevisionProjection(3, 1));

    // Act
    EstadisticaRevision e = service().obtenerEstadisticaRevisiones();

    // Assert
    assertEquals(3, e.totalPedidos());
    assertEquals(1, e.totalRevisados());
    assertEquals(2, e.totalPendientes());
    assertEquals(new BigDecimal("33.33"), e.porcentajeRevisado());
  }

  @Test
  @DisplayName("Estadisticas: sin pedidos el porcentaje es 0.00 (sin division por cero)")
  void estadisticasSinPedidos() {
    // Arrange
    when(repository.contarRevisiones()).thenReturn(new ConteoRevisionProjection(0, 0));

    // Act + Assert
    assertEquals(new BigDecimal("0.00"), service().obtenerEstadisticaRevisiones().porcentajeRevisado());
  }

  @Test
  @DisplayName("Listado: traduce el estado a filtro booleano")
  void traduceEstadoAFiltro() {
    // Arrange
    when(repository.listarRevisiones(true)).thenReturn(List.of());
    when(repository.listarRevisiones(false)).thenReturn(List.of());
    when(repository.listarRevisiones(null)).thenReturn(List.of());

    // Act
    service().listarRevisiones("revisado");
    service().listarRevisiones("PENDIENTE");
    service().listarRevisiones(null);

    // Assert
    verify(repository).listarRevisiones(true);
    verify(repository).listarRevisiones(false);
    verify(repository).listarRevisiones(null);
  }

  @Test
  @DisplayName("Listado: estado invalido lanza excepcion y no consulta la base")
  void estadoInvalidoNoConsultaBase() {
    // Act + Assert
    assertThrows(EstadoRevisionInvalidoException.class, () -> service().listarRevisiones("OTRO"));
    verify(repository, never()).listarRevisiones(org.mockito.ArgumentMatchers.any());
  }
}
