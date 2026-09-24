package pe.edu.isil.inventario.dto;

public class ReservaStockResponse {
  private Long productoId;
  private String codigo;
  private int cantidadReservada;
  private int stockRestante;
  private Long version;

  public ReservaStockResponse() {
  }

  public ReservaStockResponse(
      Long productoId,
      String codigo,
      int cantidadReservada,
      int stockRestante,
      Long version
  ) {
    this.productoId = productoId;
    this.codigo = codigo;
    this.cantidadReservada = cantidadReservada;
    this.stockRestante = stockRestante;
    this.version = version;
  }

  public Long getProductoId() {
    return productoId;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getCantidadReservada() {
    return cantidadReservada;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public Long getVersion() {
    return version;
  }
}
