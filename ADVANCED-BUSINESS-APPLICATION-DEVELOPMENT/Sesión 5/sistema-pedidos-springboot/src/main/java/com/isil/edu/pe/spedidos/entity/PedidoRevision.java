package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Evidencia de trazabilidad de la revisión de un pedido (PA4).
 *
 * <p>Esta entidad se utiliza únicamente para que Hibernate genere el esquema
 * (clave foránea + restricción UNIQUE sobre {@code pedido_id}) tanto en MySQL como
 * en H2. Las escrituras se realizan con Spring JDBC en
 * {@code PedidoRevisionJdbcRepository}.</p>
 */
@Entity
@Table(
    name = "pedido_revision",
    uniqueConstraints = @UniqueConstraint(name = "uk_pedido_revision_pedido", columnNames = "pedido_id"))
public class PedidoRevision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "pedido_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_pedido_revision_pedido"))
  private Pedido pedido;

  @Column(nullable = false, length = 255)
  private String observacion;

  @Column(name = "fecha_revision", nullable = false)
  private LocalDateTime fechaRevision;

  public PedidoRevision() {
  }

  public Long getId() { return id; }
  public Pedido getPedido() { return pedido; }
  public String getObservacion() { return observacion; }
  public LocalDateTime getFechaRevision() { return fechaRevision; }
}
