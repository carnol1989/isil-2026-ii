package pe.edu.isil.pedidos.dto;

import java.math.BigDecimal;

/**
 * DTO de presentación: combina catálogo/precio local con stock remoto.
 */
public class ProductoVista {

  private final Long id;
  private final String codigo;
  private final String nombre;
  private final BigDecimal precio;
  private final int stock;

  public ProductoVista(Long id, String codigo, String nombre, BigDecimal precio, int stock) {
    this.id = id;
    this.codigo = codigo;
    this.nombre = nombre;
    this.precio = precio;
    this.stock = stock;
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

  public int getStock() {
    return stock;
  }
}
