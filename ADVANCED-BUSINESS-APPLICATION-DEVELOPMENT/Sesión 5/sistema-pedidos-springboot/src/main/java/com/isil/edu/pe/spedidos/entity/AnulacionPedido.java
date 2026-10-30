package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Registro de auditoría local de una anulación confirmada.
 *
 * <p>La restricción UNIQUE sobre {@code pedido_id} evita un segundo registro
 * local para el mismo pedido en el flujo secuencial evaluado.</p>
 */
@Entity
@Table(
    name = "anulacion_pedido",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_anulacion_pedido_pedido_id",
        columnNames = "pedido_id"
    )
)
public class AnulacionPedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pedido_id", nullable = false)
  private Long pedidoId;

  @Column(name = "producto_codigo", nullable = false, length = 30)
  private String productoCodigo;

  @Column(name = "cantidad_devuelta", nullable = false)
  private int cantidadDevuelta;

  @Column(nullable = false)
  private LocalDateTime fecha;

  @Column(length = 250)
  private String motivo;

  protected AnulacionPedido() {
  }

  public AnulacionPedido(
      Long pedidoId,
      String productoCodigo,
      int cantidadDevuelta,
      String motivo
  ) {
    this.pedidoId = pedidoId;
    this.productoCodigo = productoCodigo;
    this.cantidadDevuelta = cantidadDevuelta;
    this.motivo = motivo == null || motivo.isBlank() ? null : motivo.trim();
    this.fecha = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Long getPedidoId() {
    return pedidoId;
  }

  public String getProductoCodigo() {
    return productoCodigo;
  }

  public int getCantidadDevuelta() {
    return cantidadDevuelta;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public String getMotivo() {
    return motivo;
  }
}
