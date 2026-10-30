package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoServiceImplTest {

  private PedidoRepository pedidoRepository;
  private InventarioClient inventarioClient;
  private DevolucionStockClient devolucionStockClient;
  private FalloLocalRegistroPedidoSimulator falloLocalSimulator;
  private PedidoServiceImpl pedidoService;

  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    inventarioClient = mock(InventarioClient.class);
    devolucionStockClient = mock(DevolucionStockClient.class);
    falloLocalSimulator = mock(FalloLocalRegistroPedidoSimulator.class);

    pedidoService = new PedidoServiceImpl(
        pedidoRepository,
        inventarioClient,
        devolucionStockClient,
        falloLocalSimulator
    );
  }

  @Test
  void debeRegistrarPedidoYCalcularTotal() {
    ReservaStockResponse reserva = nuevaReserva("LAP-001", "Laptop", new BigDecimal("2500.00"));
    when(inventarioClient.reservarStock("LAP-001", 2)).thenReturn(reserva);
    when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Pedido pedido = pedidoService.registrarPedido("Ana Torres", "LAP-001", 2);

    assertEquals("Ana Torres", pedido.getCliente());
    assertEquals("LAP-001", pedido.getProductoCodigo());
    assertEquals(new BigDecimal("5000.00"), pedido.getTotal());
    verify(inventarioClient).reservarStock("LAP-001", 2);
    verify(pedidoRepository).save(any(Pedido.class));
    verify(devolucionStockClient, never()).devolverStock(any(), org.mockito.ArgumentMatchers.anyInt());
  }

  @Test
  void debeRechazarCantidadMenorOIgualACero() {
    PedidoException error = assertThrows(
        PedidoException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 0)
    );

    assertEquals("La cantidad debe ser mayor que cero.", error.getMessage());
    verifyNoInteractions(inventarioClient, pedidoRepository, devolucionStockClient);
  }

  @Test
  void debeConservarCausaDelErrorFuncionalDeInventario() {
    InventarioClientException causa = new InventarioClientException(409, "Stock insuficiente.");
    when(inventarioClient.reservarStock("LAP-001", 5)).thenThrow(causa);

    PedidoException error = assertThrows(
        PedidoException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 5)
    );

    assertEquals("Stock insuficiente.", error.getMessage());
    assertSame(causa, error.getCause());
    verifyNoInteractions(pedidoRepository, devolucionStockClient);
  }

  @Test
  void debeTratarError5xxComoServicioNoDisponible() {
    InventarioClientException causa = new InventarioClientException(500, "Error interno de inventario.");
    when(inventarioClient.reservarStock("LAP-001", 1)).thenThrow(causa);

    InventarioNoDisponibleException error = assertThrows(
        InventarioNoDisponibleException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 1)
    );

    assertSame(causa, error.getCause());
    verifyNoInteractions(pedidoRepository, devolucionStockClient);
  }

  @Test
  @DisplayName("Compensate remote stock when local failure occurs after confirmed reservation")
  void compensateRemoteStockWhenLocalFailureOccursAfterConfirmedReservation() {
    ReservaStockResponse reserva = nuevaReserva("LAP-001", "Laptop", new BigDecimal("2500.00"));
    when(inventarioClient.reservarStock("LAP-001", 2)).thenReturn(reserva);
    when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenReturn(new DevolucionStockResponse("LAP-001", 2, 10));
    doThrow(new IllegalStateException("Fallo local controlado."))
        .when(falloLocalSimulator).verificarAntesDelCommit();

    assertThrows(
        IllegalStateException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 2)
    );

    verify(inventarioClient).reservarStock("LAP-001", 2);
    verify(pedidoRepository).save(any(Pedido.class));
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
  }

  @Test
  @DisplayName("Keep reconciliation evidence when compensation cannot be confirmed")
  void keepReconciliationEvidenceWhenCompensationCannotBeConfirmed() {
    ReservaStockResponse reserva = nuevaReserva("LAP-001", "Laptop", new BigDecimal("2500.00"));
    when(inventarioClient.reservarStock("LAP-001", 2)).thenReturn(reserva);
    when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    doThrow(new IllegalStateException("Fallo local controlado."))
        .when(falloLocalSimulator).verificarAntesDelCommit();
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("Timeout durante compensación."));

    IllegalStateException error = assertThrows(
        IllegalStateException.class,
        () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 2)
    );

    assertEquals(1, error.getSuppressed().length);
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
  }

  private ReservaStockResponse nuevaReserva(String codigo, String nombre, BigDecimal precio) {
    ReservaStockResponse reserva = new ReservaStockResponse();
    reserva.setCodigo(codigo);
    reserva.setNombre(nombre);
    reserva.setPrecio(precio);
    return reserva;
  }
}
