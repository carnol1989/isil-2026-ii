package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.AnulacionPedidoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta la anulación local y la devolución remota de stock.
 *
 * <p>La transacción Spring cubre únicamente H2. La devolución remota ocurre
 * en otra aplicación y, por tanto, no forma una transacción distribuida.</p>
 */
@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

  private final PedidoRepository pedidoRepository;
  private final AnulacionPedidoRepository anulacionPedidoRepository;
  private final DevolucionStockClient devolucionStockClient;

  public AnulacionPedidoServiceImpl(
      PedidoRepository pedidoRepository,
      AnulacionPedidoRepository anulacionPedidoRepository,
      DevolucionStockClient devolucionStockClient
  ) {
    this.pedidoRepository = pedidoRepository;
    this.anulacionPedidoRepository = anulacionPedidoRepository;
    this.devolucionStockClient = devolucionStockClient;
  }

  @Override
  @Transactional
  public AnulacionPedido anular(Long pedidoId, String motivo) {
    if (pedidoId == null || pedidoId <= 0) {
      throw new AnulacionPedidoException(
          AnulacionPedidoException.Tipo.PEDIDO_NO_ENCONTRADO,
          "El pedido solicitado no existe."
      );
    }

    if (anulacionPedidoRepository.existsByPedidoId(pedidoId)) {
      throw duplicada(pedidoId);
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new AnulacionPedidoException(
            AnulacionPedidoException.Tipo.PEDIDO_NO_ENCONTRADO,
            "No existe el pedido con id " + pedidoId + "."
        ));

    if (pedido.getEstado() != PedidoEstado.REGISTRADO) {
      throw duplicada(pedidoId);
    }

    final DevolucionStockResponse devolucion;
    try {
      devolucion = devolucionStockClient.devolverStock(
          pedido.getProductoCodigo(),
          pedido.getCantidad()
      );
    } catch (InventarioClientException e) {
      throw new AnulacionPedidoException(
          AnulacionPedidoException.Tipo.ERROR_INVENTARIO,
          "Inventario rechazó la devolución: " + e.getMessage(),
          e
      );
    }

    if (devolucion == null
        || devolucion.codigo() == null
        || !pedido.getProductoCodigo().equals(devolucion.codigo())
        || devolucion.cantidadDevuelta() != pedido.getCantidad()) {
      throw new AnulacionPedidoException(
          AnulacionPedidoException.Tipo.RESPUESTA_INVENTARIO_INVALIDA,
          "La confirmación de devolución recibida desde Inventario es inconsistente."
      );
    }

    AnulacionPedido anulacion = new AnulacionPedido(
        pedido.getId(),
        pedido.getProductoCodigo(),
        pedido.getCantidad(),
        motivo
    );

    pedido.setEstado(PedidoEstado.ANULADO);
    pedidoRepository.save(pedido);
    return anulacionPedidoRepository.save(anulacion);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AnulacionPedido> listar() {
    return anulacionPedidoRepository.findAllByOrderByIdDesc();
  }

  private static AnulacionPedidoException duplicada(Long pedidoId) {
    return new AnulacionPedidoException(
        AnulacionPedidoException.Tipo.ANULACION_DUPLICADA,
        "El pedido " + pedidoId + " ya fue anulado."
    );
  }
}
