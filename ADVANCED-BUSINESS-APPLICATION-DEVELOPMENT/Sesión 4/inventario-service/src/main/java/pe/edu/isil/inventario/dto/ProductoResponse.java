package pe.edu.isil.inventario.dto;

public class ProductoResponse {
  private Long id;
  private String codigo;
  private String nombre;
  private int stock;
  private Long version;

  public ProductoResponse() {
  }

  public ProductoResponse(Long id, String codigo, String nombre, int stock, Long version) {
    this.id = id;
    this.codigo = codigo;
    this.nombre = nombre;
    this.stock = stock;
    this.version = version;
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

  public int getStock() {
    return stock;
  }

  public Long getVersion() {
    return version;
  }
}
