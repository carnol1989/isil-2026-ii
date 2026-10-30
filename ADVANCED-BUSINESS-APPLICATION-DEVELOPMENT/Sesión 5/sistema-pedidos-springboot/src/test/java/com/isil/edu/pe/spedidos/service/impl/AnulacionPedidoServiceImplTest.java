package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionPedidoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas PA2 con JUnit 5 + Mockito, sin iniciar Spring ni Inventario. */
class AnulacionPedidoServiceImplTest {

  private PedidoRepository pedidoRepository;
  private AnulacionPedidoRepository anulacionRepository;
  private DevolucionStockClient devolucionStockClient;
  private AnulacionPedidoServiceImpl service;

  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    anulacionRepository = mock(AnulacionPedidoRepository.class);
    devolucionStockClient = mock(DevolucionStockClient.class);
    service = new AnulacionPedidoServiceImpl(
        pedidoRepository,
        anulacionRepository,
        devolucionStockClient
    );
  }

  @Test
  @DisplayName("Anulate order when order is registered and remote stock return succeeds")
  void anulateOrderWhenOrderIsRegisteredAndRemoteStockReturnSucceeds() {
    Pedido pedido = pedidoRegistrado(1L, "LAP-001", 2);
    when(anulacionRepository.existsByPedidoId(1L)).thenReturn(false);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenReturn(new DevolucionStockResponse("LAP-001", 2, 10));
    when(anulacionRepository.save(org.mockito.ArgumentMatchers.any(AnulacionPedido.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AnulacionPedido anulacion = service.anular(1L, "Solicitud del cliente");

    assertEquals(PedidoEstado.ANULADO, pedido.getEstado());
    assertEquals(1L, anulacion.getPedidoId());
    assertEquals(2, anulacion.getCantidadDevuelta());
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
    verify(pedidoRepository).save(pedido);
    verify(anulacionRepository).save(org.mockito.ArgumentMatchers.any(AnulacionPedido.class));
  }

  @Test
  @DisplayName("Reject duplicate cancellation when order was already cancelled")
  void rejectDuplicateCancellationWhenOrderWasAlreadyCancelled() {
    when(anulacionRepository.existsByPedidoId(1L)).thenReturn(true);

    AnulacionPedidoException error = assertThrows(
        AnulacionPedidoException.class,
        () -> service.anular(1L, "Segundo intento")
    );

    assertEquals(AnulacionPedidoException.Tipo.ANULACION_DUPLICADA, error.getTipo());
    verify(devolucionStockClient, never()).devolverStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
    verify(anulacionRepository, never()).save(org.mockito.ArgumentMatchers.any(AnulacionPedido.class));
  }

  @Test
  @DisplayName("Keep local order unchanged when inventory service is unavailable")
  void keepLocalOrderUnchangedWhenInventoryServiceIsUnavailable() {
    Pedido pedido = pedidoRegistrado(1L, "LAP-001", 2);
    when(anulacionRepository.existsByPedidoId(1L)).thenReturn(false);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("Inventario fuera de línea."));

    assertThrows(
        InventarioNoDisponibleException.class,
        () -> service.anular(1L, "Solicitud del cliente")
    );

    assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado());
    verify(pedidoRepository, never()).save(org.mockito.ArgumentMatchers.any(Pedido.class));
    verify(anulacionRepository, never()).save(org.mockito.ArgumentMatchers.any(AnulacionPedido.class));
  }

  private Pedido pedidoRegistrado(Long id, String codigo, int cantidad) {
    Pedido pedido = new Pedido(
        id,
        "Ana Torres",
        codigo,
        "Laptop",
        new BigDecimal("2500.00"),
        cantidad,
        new BigDecimal("5000.00"),
        LocalDateTime.now()
    );
    pedido.setEstado(PedidoEstado.REGISTRADO);
    return pedido;
  }
}
