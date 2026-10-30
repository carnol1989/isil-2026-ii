package pe.edu.isil.pedidos.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import pe.edu.isil.pedidos.exception.PedidoException;
import pe.edu.isil.pedidos.exception.PedidoNoEncontradoException;

/**
 * Servicio EJB que concentra la lógica de negocio de los pedidos.
 *
 * <p>Las operaciones que modifican pedido y stock se ejecutan dentro de una transacción JTA.
 * Si una validación genera {@link PedidoException}, la transacción se revierte y se evita
 * dejar cambios parciales en la base de datos.</p>
 */
@Stateless
public class PedidoService {

  @PersistenceContext(unitName = "PedidosPU")
  private EntityManager entityManager;

  /**
   * Registra un nuevo pedido, descuenta el stock y calcula el total.
   *
   * @param cliente nombre del cliente
   * @param productoId identificador del producto
   * @param cantidad cantidad solicitada
   * @return pedido registrado
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido registrarPedido(String cliente, Long productoId, int cantidad) {
    validarDatos(cliente, productoId, cantidad);

    Producto producto = buscarProducto(productoId);
    descontarStockControlado(producto, cantidad);

    BigDecimal total = calcularTotal(producto, cantidad);
    Pedido pedido = new Pedido(cliente.trim(), producto, cantidad, total);
    entityManager.persist(pedido);
    return pedido;
  }

  /**
   * Busca un pedido por su identificador.
   *
   * @param pedidoId identificador del pedido
   * @return pedido administrado por JPA
   * @throws PedidoNoEncontradoException si el pedido no existe
   */
  @TransactionAttribute(TransactionAttributeType.SUPPORTS)
  public Pedido buscarPedido(Long pedidoId) {
    if (pedidoId == null) {
      throw new PedidoException("El ID del pedido es obligatorio.");
    }

    Pedido pedido = entityManager.find(Pedido.class, pedidoId);
    if (pedido == null) {
      throw new PedidoNoEncontradoException("No existe el pedido con ID " + pedidoId + ".");
    }
    return pedido;
  }

  /**
   * Actualiza un pedido conservando la consistencia del stock.
   *
   * <p>Si el producto no cambia, solo se aplica al inventario la diferencia entre la cantidad
   * nueva y la cantidad anterior. Si el producto cambia, primero se repone el stock del producto
   * original y luego se reserva stock del nuevo producto. Toda la operación es atómica.</p>
   *
   * @param pedidoId identificador del pedido a modificar
   * @param cliente nuevo nombre del cliente
   * @param productoId identificador del producto seleccionado
   * @param cantidad nueva cantidad
   * @return pedido actualizado
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Pedido actualizarPedido(Long pedidoId, String cliente, Long productoId, int cantidad) {
    validarDatos(cliente, productoId, cantidad);

    Pedido pedido = buscarPedido(pedidoId);
    Producto productoOriginal = pedido.getProducto();
    int cantidadOriginal = pedido.getCantidad();

    if (productoOriginal.getId().equals(productoId)) {
      ajustarStockMismoProducto(productoOriginal, cantidadOriginal, cantidad);
      BigDecimal total = calcularTotal(productoOriginal, cantidad);
      pedido.actualizar(cliente.trim(), productoOriginal, cantidad, total);
      return pedido;
    }

    Producto productoNuevo = buscarProducto(productoId);

    // La devolución y el nuevo descuento forman parte de la misma transacción.
    productoOriginal.reponerStock(cantidadOriginal);
    descontarStockControlado(productoNuevo, cantidad);

    BigDecimal total = calcularTotal(productoNuevo, cantidad);
    pedido.actualizar(cliente.trim(), productoNuevo, cantidad, total);
    return pedido;
  }

  /**
   * Elimina un pedido y devuelve al inventario las unidades que estaban reservadas.
   *
   * @param pedidoId identificador del pedido a eliminar
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void eliminarPedido(Long pedidoId) {
    Pedido pedido = buscarPedido(pedidoId);
    pedido.getProducto().reponerStock(pedido.getCantidad());
    entityManager.remove(pedido);
  }

  /**
   * Lista los productos disponibles. En la primera ejecución crea los productos de ejemplo.
   *
   * @return productos ordenados por ID
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public List<Producto> listarProductos() {
    inicializarProductosSiEsNecesario();
    return entityManager
        .createQuery(
            """
            select p
            from Producto p
            order by p.id
            """,
            Producto.class)
        .getResultList();
  }

  /**
   * Lista los pedidos e inicializa también su producto asociado para la vista JSP.
   *
   * @return pedidos ordenados del más reciente al más antiguo
   */
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
            Pedido.class)
        .getResultList();
  }

  private void validarDatos(String cliente, Long productoId, int cantidad) {
    if (cliente == null || cliente.isBlank()) {
      throw new PedidoException("El cliente es obligatorio.");
    }
    if (cliente.trim().length() > 120) {
      throw new PedidoException("El cliente no puede superar 120 caracteres.");
    }
    if (productoId == null) {
      throw new PedidoException("Debe seleccionar un producto.");
    }
    if (cantidad <= 0) {
      throw new PedidoException("La cantidad debe ser mayor que cero.");
    }
  }

  private Producto buscarProducto(Long productoId) {
    Producto producto = entityManager.find(Producto.class, productoId);
    if (producto == null) {
      throw new PedidoException("El producto seleccionado no existe.");
    }
    return producto;
  }

  private void ajustarStockMismoProducto(Producto producto, int cantidadAnterior, int cantidadNueva) {
    int diferencia = cantidadNueva - cantidadAnterior;

    if (diferencia > 0) {
      descontarStockControlado(producto, diferencia);
    } else if (diferencia < 0) {
      producto.reponerStock(Math.abs(diferencia));
    }
  }

  private void descontarStockControlado(Producto producto, int cantidad) {
    try {
      producto.descontarStock(cantidad);
    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new PedidoException(e.getMessage());
    }
  }

  private BigDecimal calcularTotal(Producto producto, int cantidad) {
    return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
  }

  private void inicializarProductosSiEsNecesario() {
    Long cantidad = entityManager
        .createQuery(
            """
            select count(p)
            from Producto p
            """,
            Long.class)
        .getSingleResult();

    if (cantidad == 0) {
      entityManager.persist(new Producto("Laptop", new BigDecimal("2500.00"), 5));
      entityManager.persist(new Producto("Monitor", new BigDecimal("850.00"), 8));
      entityManager.persist(new Producto("Teclado", new BigDecimal("120.00"), 15));
    }
  }
}
