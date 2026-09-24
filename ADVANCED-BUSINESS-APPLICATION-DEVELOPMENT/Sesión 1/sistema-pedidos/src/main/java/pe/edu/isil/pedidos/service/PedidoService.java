package pe.edu.isil.pedidos.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import pe.edu.isil.pedidos.dto.ProductoVista;
import pe.edu.isil.pedidos.integration.InventarioClient;
import pe.edu.isil.pedidos.integration.InventarioClientException;
import pe.edu.isil.pedidos.integration.dto.ProductoInventarioResponse;

/**
 * Servicio EJB que maneja la lógica de negocio relacionada con los pedidos.
 */
@Stateless
public class PedidoService {

  @PersistenceContext(unitName = "PedidosPU")
  private EntityManager entityManager;

  @EJB
  private InventarioClient inventarioClient;

  /**
   * Registra un nuevo pedido.
   *
   * <p>El stock ya no se descuenta en H2. Se reserva mediante inventario-service.
   * La reserva remota y el INSERT local son transacciones independientes; este
   * punto se documenta como una frontera transaccional distribuida de la sesión.</p>
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido registrarPedido(String cliente, Long productoId, int cantidad) {
    validarDatos(cliente, productoId, cantidad);

    Producto producto = entityManager.find(Producto.class, productoId);
    if (producto == null) {
      throw new PedidoException("El producto no existe.");
    }

    try {
      // inventario-service valida disponibilidad y descuenta en SQL Server.
      inventarioClient.reservarStock(producto.getCodigo(), cantidad);
    } catch (InventarioClientException e) {
      // 400/404/409 son errores controlados del servicio remoto y se muestran
      // como errores de negocio en el Sistema de Pedidos.
      throw new PedidoException(e.getMessage());
    }

    BigDecimal total = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    Pedido pedido = new Pedido(cliente.trim(), producto, cantidad, total);
    entityManager.persist(pedido);
    return pedido;
  }

  /**
   * Combina el catálogo/precio local con el stock remoto.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<ProductoVista> listarProductos() {
    inicializarProductosSiEsNecesario();

    List<Producto> productosLocales = entityManager
        .createQuery(
            """
            select p
            from Producto p
            order by p.id
            """,
            Producto.class
        )
        .getResultList();

    List<ProductoInventarioResponse> inventario = inventarioClient.listarProductos();

    Map<String, ProductoInventarioResponse> inventarioPorCodigo = inventario.stream()
        .collect(Collectors.toMap(
            ProductoInventarioResponse::getCodigo,
            Function.identity(),
            (primero, segundo) -> primero
        ));

    return productosLocales.stream()
        .map(producto -> {
          ProductoInventarioResponse remoto = inventarioPorCodigo.get(producto.getCodigo());
          int stock = remoto != null ? remoto.getStock() : 0;

          return new ProductoVista(
              producto.getId(),
              producto.getCodigo(),
              producto.getNombre(),
              producto.getPrecio(),
              stock
          );
        })
        .toList();
  }

  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public List<Pedido> listarPedidos() {
    return entityManager
        .createQuery(
            """
            select p
            from Pedido p
            join fetch p.producto
            order by p.id desc
            """,
            Pedido.class
        )
        .getResultList();
  }

  private void validarDatos(String cliente, Long productoId, int cantidad) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException("El cliente es obligatorio.");
    }
    if (productoId == null) {
      throw new PedidoException("Debe seleccionar un producto.");
    }
    if (cantidad <= 0) {
      throw new PedidoException("La cantidad debe ser mayor que cero.");
    }
  }

  /**
   * Inicializa únicamente el catálogo local (código, nombre, precio).
   * Los mismos códigos deben existir en inventario-service.
   */
  private void inicializarProductosSiEsNecesario() {
    Long cantidad = entityManager
        .createQuery(
            """
            select count(p)
            from Producto p
            """,
            Long.class
        )
        .getSingleResult();

    if (cantidad == 0) {
      entityManager.persist(new Producto("LAP-001", "Laptop", new BigDecimal("2500.00")));
      entityManager.persist(new Producto("MON-001", "Monitor", new BigDecimal("850.00")));
      entityManager.persist(new Producto("TEC-001", "Teclado", new BigDecimal("120.00")));
    }
  }
}
