package pe.edu.isil.pedidos.integration.dto;

/**
 * Contrato recibido después de reservar stock.
 */
public class ReservaStockResponse {

  private Long productoId;
  private String codigo;
  private int cantidadReservada;
  private int stockRestante;
  private Long version;

  public ReservaStockResponse() {
  }

  public Long getProductoId() {
    return productoId;
  }

  public void setProductoId(Long productoId) {
    this.productoId = productoId;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public int getCantidadReservada() {
    return cantidadReservada;
  }

  public void setCantidadReservada(int cantidadReservada) {
    this.cantidadReservada = cantidadReservada;
  }

  public int getStockRestante() {
    return stockRestante;
  }

  public void setStockRestante(int stockRestante) {
    this.stockRestante = stockRestante;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
