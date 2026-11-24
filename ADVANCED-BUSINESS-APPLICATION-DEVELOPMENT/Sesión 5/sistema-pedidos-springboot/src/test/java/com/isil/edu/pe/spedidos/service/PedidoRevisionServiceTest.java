package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PedidoRevisionServiceTest {

  @Autowired private PedidoRevisionService service;
  @Autowired private PedidoRepository pedidoRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void limpiarAntes() {
    limpiar();
  }

  @AfterEach
  void limpiarDespues() {
    limpiar();
  }

  private void limpiar() {
    jdbcTemplate.update("DELETE FROM pedido_revision");
    pedidoRepository.deleteAll();
  }

  @Test
  @DisplayName("Commit: una revision valida marca el pedido y registra una sola fila de trazabilidad")
  void commitMarcaPedidoYRegistraRevision() {
    // Arrange
    Pedido pedido = crearPedido();

    // Act
    service.revisarPedido(pedido.getId(), "  Datos verificados  ");

    // Assert
    assertTrue(estaRevisado(pedido.getId()));
    assertEquals(1, contarRevisiones(pedido.getId()));
    String observacion = jdbcTemplate.queryForObject(
        "SELECT observacion FROM pedido_revision WHERE pedido_id = ?", String.class, pedido.getId());
    assertEquals("Datos verificados", observacion);
  }

  @Test
  @DisplayName("Revision duplicada: se rechaza y no crea otra fila ni altera datos")
  void revisionDuplicadaSeRechazaSinCambios() {
    // Arrange
    Pedido pedido = crearPedido();
    service.revisarPedido(pedido.getId(), "Primera revision");

    // Act + Assert
    assertThrows(PedidoYaRevisadoException.class,
        () -> service.revisarPedido(pedido.getId(), "Segunda revision"));
    assertEquals(1, contarRevisiones(pedido.getId()));
    assertTrue(estaRevisado(pedido.getId()));
  }

  @Test
  @DisplayName("Pedido inexistente: respuesta controlada y ninguna fila de revision")
  void pedidoInexistenteLanzaExcepcionControlada() {
    // Act + Assert
    assertThrows(PedidoNoEncontradoException.class, () -> service.revisarPedido(999_999L, "Obs"));
    assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pedido_revision", Integer.class));
  }

  @Test
  @DisplayName("Validaciones: id invalido u observacion vacia no ejecutan escrituras")
  void validacionesNoEjecutanEscrituras() {
    // Arrange
    Pedido pedido = crearPedido();

    // Act + Assert
    assertThrows(PedidoException.class, () -> service.revisarPedido(0, "Obs"));
    assertThrows(PedidoException.class, () -> service.revisarPedido(-5, "Obs"));
    assertThrows(PedidoException.class, () -> service.revisarPedido(pedido.getId(), "   "));
    assertThrows(PedidoException.class, () -> service.revisarPedido(pedido.getId(), null));
    assertFalse(estaRevisado(pedido.getId()));
    assertEquals(0, contarRevisiones(pedido.getId()));
  }

  private Pedido crearPedido() {
    return pedidoRepository.saveAndFlush(new Pedido(
        "Ana Torres", "LAP-001", "Laptop", new BigDecimal("100.00"), 2, new BigDecimal("200.00")));
  }

  private boolean estaRevisado(Long id) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject("SELECT revisado FROM pedido WHERE id = ?", Boolean.class, id));
  }

  private int contarRevisiones(Long id) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?", Integer.class, id);
  }
}
