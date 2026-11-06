package pe.edu.isil.inventario.dto;

/**
 * DTO de salida de una devolución de stock.
 *
 * <p>El contrato expone únicamente datos de transporte y no la entidad JPA.</p>
 */
public class DevolucionStockResponse {

  private String codigo;
  private int cantidadDevuelta;
  private int stockRestante;

  public DevolucionStockResponse() {
  }

  public DevolucionStockResponse(String codigo, int cantidadDevuelta, int stockRestante) {
    this.codigo = codigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.stockRestante = stockRestante;
  }

  public String getCodigo() {
    return codigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public int getStockRestante() {
    return stockRestante;
  }
}
