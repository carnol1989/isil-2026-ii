package pe.edu.isil.pedidos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido persistido localmente.
 *
 * <p>Guarda un snapshot del producto recibido desde inventario-service. De esta
 * forma un cambio posterior de nombre o precio en el catálogo no altera el
 * histórico del pedido.</p>
 */
@Entity
@Table(name = "pedido")
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String cliente;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(name = "producto_nombre", nullable = false, length = 120)
  private String productoNombre;

  @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
  private BigDecimal precioUnitario;

  @Column(nullable = false)
  private int cantidad;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(nullable = false)
  private LocalDateTime fecha;

  protected Pedido() {
    // Requerido por Jakarta Persistence.
  }

  public Pedido(
      String cliente,
      String productoCodigo,
      String productoNombre,
      BigDecimal precioUnitario,
      int cantidad,
      BigDecimal total
  ) {
    this.cliente = cliente;
    this.productoCodigo = productoCodigo;
    this.productoNombre = productoNombre;
    this.precioUnitario = precioUnitario;
    this.cantidad = cantidad;
    this.total = total;
    this.fecha = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getCliente() {
    return cliente;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public String getProductoNombre() {
    return productoNombre;
  }

  public BigDecimal getPrecioUnitario() {
    return precioUnitario;
  }

  public int getCantidad() {
    return cantidad;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }
}
