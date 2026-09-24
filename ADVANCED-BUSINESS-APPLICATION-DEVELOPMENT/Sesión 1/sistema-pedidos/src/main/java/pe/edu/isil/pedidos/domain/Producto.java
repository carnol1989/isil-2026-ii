package pe.edu.isil.pedidos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Catálogo local del Sistema de Pedidos.
 *
 * <p>El stock ya no se almacena aquí. La fuente oficial del stock es
 * inventario-service. El campo {@code codigo} actúa como identificador de
 * negocio compartido entre ambos sistemas.</p>
 */
@Entity
@Table(name = "producto")
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 30)
  private String codigo;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal precio;

  protected Producto() {
    // Constructor requerido por JPA.
  }

  public Producto(String codigo, String nombre, BigDecimal precio) {
    this.codigo = codigo;
    this.nombre = nombre;
    this.precio = precio;
  }

  public Long getId() {
    return id;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public BigDecimal getPrecio() {
    return precio;
  }
}
