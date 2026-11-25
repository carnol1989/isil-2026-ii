package pe.edu.isil.pedidos.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.integration.InventarioClient;
import pe.edu.isil.pedidos.integration.InventarioClientException;
import pe.edu.isil.pedidos.integration.dto.ProductoInventarioResponse;
import pe.edu.isil.pedidos.integration.dto.ReservaStockResponse;

/**
 * Servicio EJB responsable de los pedidos.
 *
 * <p>El catálogo, precio y stock pertenecen a inventario-service. Esta
 * aplicación solo conserva un snapshot de los datos del producto dentro del
 * pedido histórico.</p>
 */
@Stateless
public class PedidoService {

  @PersistenceContext(unitName = "PedidosPU")
  private EntityManager entityManager;

  @EJB
  private InventarioClient inventarioClient;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido registrarPedido(String cliente, String productoCodigo, int cantidad) {
    validarDatos(cliente, productoCodigo, cantidad);

    final ReservaStockResponse reserva;
    try {
      // Una sola operación remota valida disponibilidad y descuenta el stock.
      reserva = inventarioClient.reservarStock(productoCodigo.trim(), cantidad);
    } catch (InventarioClientException e) {
      // 400/404/409 se presentan como errores de negocio del formulario.
      throw new PedidoException(e.getMessage());
    }

    validarReserva(reserva);

    BigDecimal total = reserva.getPrecio().multiply(BigDecimal.valueOf(cantidad));

    Pedido pedido = new Pedido(
        cliente.trim(),
        reserva.getCodigo(),
        reserva.getNombre(),
        reserva.getPrecio(),
        cantidad,
        total
    );

    entityManager.persist(pedido);
    return pedido;
  }

  /**
   * El combo de productos se obtiene directamente desde inventario-service.
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<ProductoInventarioResponse> listarProductos() {
    return inventarioClient.listarProductos();
  }

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Pedido> listarPedidos() {
    return entityManager
        .createQuery(
            """
            select p
            from Pedido p
            order by p.id desc
            """,
            Pedido.class
        )
        .getResultList();
  }

  private void validarDatos(String cliente, String productoCodigo, int cantidad) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException("El cliente es obligatorio.");
    }
    if (productoCodigo == null || productoCodigo.isBlank()) {
      throw new PedidoException("Debe seleccionar un producto.");
    }
    if (cantidad <= 0) {
      throw new PedidoException("La cantidad debe ser mayor que cero.");
    }
  }

  private void validarReserva(ReservaStockResponse reserva) {
    if (reserva == null
        || reserva.getCodigo() == null
        || reserva.getCodigo().isBlank()
        || reserva.getNombre() == null
        || reserva.getNombre().isBlank()
        || reserva.getPrecio() == null) {
      throw new PedidoException(
          "inventario-service devolvió una respuesta incompleta para registrar el pedido."
      );
    }
  }
}
